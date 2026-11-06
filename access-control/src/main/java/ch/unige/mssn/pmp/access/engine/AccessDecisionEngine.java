package ch.unige.mssn.pmp.access.engine;

import ch.unige.mssn.pmp.access.engine.AccessFacts.EmergencyMode;
import ch.unige.mssn.pmp.access.engine.AccessFacts.Grant;
import ch.unige.mssn.pmp.access.engine.AccessFacts.Mandate;
import ch.unige.mssn.pmp.access.engine.AccessFacts.PatientSettings;
import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Outcome;
import ch.unige.mssn.pmp.access.model.ReasonCode;
import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import ch.unige.mssn.pmp.access.policy.ActionRule;
import java.util.List;
import java.util.Optional;

/**
 * Décide d'une demande en posant trois questions dans le même ordre : lien avec le patient, rôle, validité du droit.
 * Sans effet de bord et sans dépendance à Spring, pour rester vérifiable isolément.
 */
public final class AccessDecisionEngine {

    static final int MIN_JUSTIFICATION_LENGTH = 10;

    private final AccessPolicy policy;

    public AccessDecisionEngine(AccessPolicy policy) {
        this.policy = policy;
    }

    public AccessDecision decide(AccessRequest request, AccessFacts facts) {
        Optional<ActionRule> found = policy.rule(request.resource(), request.action());
        if (found.isEmpty()) {
            return AccessDecision.deny(ReasonCode.UNKNOWN_ACTION, null);
        }
        ActionRule rule = found.get();
        if (request.subject().personId() == null || request.subject().roles().isEmpty()) {
            return AccessDecision.deny(ReasonCode.INVALID_SUBJECT, null);
        }
        return rule.global() ? decideGlobal(request, rule) : decideForPatient(request, rule, facts);
    }

    private AccessDecision decideGlobal(AccessRequest request, ActionRule rule) {
        return request.subject().roles().stream()
                .filter(rule::allows)
                .findFirst()
                .map(role -> AccessDecision.permit(ReasonCode.OVERSIGHT_ROLE, role, null))
                .orElseGet(() -> AccessDecision.deny(ReasonCode.ROLE_NOT_ALLOWED, null));
    }

    private AccessDecision decideForPatient(AccessRequest request, ActionRule rule, AccessFacts facts) {
        if (request.patientId() == null) {
            return AccessDecision.deny(ReasonCode.PATIENT_UNKNOWN, null);
        }
        Optional<PatientSettings> settings = facts.patientSettings(request.patientId());
        if (settings.isEmpty()) {
            return AccessDecision.deny(ReasonCode.PATIENT_UNKNOWN, null);
        }
        AccessDecision firstDenial = null;
        for (Role role : capacities(request)) {
            AccessDecision decision = evaluate(role, request, rule, settings.get(), facts);
            if (decision.permitted()) {
                return upgradeIfEmergencyBroader(decision, request, rule, settings.get(), facts);
            }
            firstDenial = firstDenial == null ? decision : firstDenial;
        }
        if (firstDenial == null) {
            firstDenial = AccessDecision.deny(ReasonCode.NO_RELATIONSHIP, null);
        }
        return request.emergencyRequested() && reachableByEmergency(firstDenial)
                ? emergency(request, rule, settings.get(), facts, firstDenial)
                : firstDenial;
    }

    /** Sur son propre dossier, une personne agit comme patient : un médecin ne peut pas s'y servir de son rôle professionnel. */
    private List<Role> capacities(AccessRequest request) {
        if (request.subject().isSelf(request.patientId())) {
            return request.subject().roles().contains(Role.PATIENT) ? List.of(Role.PATIENT) : List.of();
        }
        return request.subject().roles().stream().filter(role -> role != Role.PATIENT).toList();
    }

    private AccessDecision evaluate(Role role, AccessRequest request, ActionRule rule, PatientSettings settings, AccessFacts facts) {
        return switch (role.category()) {
            case SELF -> rule.allows(role)
                    ? checkDataLevel(request, rule, AccessDecision.permit(ReasonCode.SELF_ACCESS, role, Confidentiality.SECRET))
                    : AccessDecision.deny(ReasonCode.ROLE_NOT_ALLOWED, role);
            case MANDATED -> evaluateRepresentative(role, request, rule, settings, facts);
            case PROFESSIONAL -> evaluateProfessional(role, request, rule, settings, facts);
            case OVERSIGHT -> rule.allows(role)
                    ? AccessDecision.permit(ReasonCode.OVERSIGHT_ROLE, role, null)
                    : AccessDecision.deny(ReasonCode.ROLE_NOT_ALLOWED, role);
        };
    }

    private AccessDecision evaluateRepresentative(Role role, AccessRequest request, ActionRule rule, PatientSettings settings, AccessFacts facts) {
        Optional<Mandate> mandate = facts.mandate(request.subject().personId(), request.patientId());
        if (mandate.isEmpty()) {
            return AccessDecision.deny(ReasonCode.NO_RELATIONSHIP, role);
        }
        if (!rule.allows(role)) {
            return AccessDecision.deny(ReasonCode.ROLE_NOT_ALLOWED, role);
        }
        if (!settings.consentActive()) {
            return AccessDecision.deny(ReasonCode.CONSENT_REVOKED, role);
        }
        if (!mandate.get().validAt(request.at())) {
            return AccessDecision.deny(ReasonCode.MANDATE_INVALID, role);
        }
        if (!mandate.get().covers(request.action())) {
            return AccessDecision.deny(ReasonCode.MANDATE_SCOPE, role);
        }
        return checkDataLevel(request, rule, AccessDecision.permit(ReasonCode.VALID_MANDATE, role, Confidentiality.SECRET));
    }

    private AccessDecision evaluateProfessional(Role role, AccessRequest request, ActionRule rule, PatientSettings settings, AccessFacts facts) {
        String person = request.subject().personId();
        if (facts.isExcluded(person, request.patientId())) {
            return AccessDecision.deny(ReasonCode.EXCLUDED_BY_PATIENT, role);
        }
        Optional<Grant> grant = facts.grant(person, request.patientId());
        if (grant.isEmpty()) {
            return AccessDecision.deny(ReasonCode.NO_RELATIONSHIP, role);
        }
        if (!rule.allows(role)) {
            return AccessDecision.deny(ReasonCode.ROLE_NOT_ALLOWED, role);
        }
        if (!settings.consentActive()) {
            return AccessDecision.deny(ReasonCode.CONSENT_REVOKED, role);
        }
        if (!grant.get().validAt(request.at())) {
            return AccessDecision.deny(ReasonCode.GRANT_EXPIRED, role);
        }
        return checkDataLevel(request, rule, AccessDecision.permit(ReasonCode.CARE_RELATIONSHIP, role, grant.get().level()));
    }

    private AccessDecision checkDataLevel(AccessRequest request, ActionRule rule, AccessDecision permit) {
        if (!rule.levelBased() && request.dataLevel() != null && !request.dataLevel().atMost(permit.clearance())) {
            return AccessDecision.deny(ReasonCode.CONFIDENTIALITY_TOO_HIGH, permit.effectiveRole());
        }
        return permit;
    }

    /** Seule l'absence de droit se rattrape : un rôle qui n'a pas l'action ou une donnée trop protégée restent refusés. */
    private boolean reachableByEmergency(AccessDecision denial) {
        return denial.reason() != ReasonCode.ROLE_NOT_ALLOWED && denial.reason() != ReasonCode.CONFIDENTIALITY_TOO_HIGH;
    }

    /** Un soignant au droit limité au niveau normal peut, en urgence, voir davantage si le patient l'a prévu. */
    private AccessDecision upgradeIfEmergencyBroader(AccessDecision decision, AccessRequest request, ActionRule rule, PatientSettings settings, AccessFacts facts) {
        if (!request.emergencyRequested() || !decision.effectiveRole().isProfessional()) {
            return decision;
        }
        AccessDecision emergency = emergency(request, rule, settings, facts, decision);
        boolean broader = emergency.outcome() == Outcome.EMERGENCY_PERMIT
                && emergency.clearance().compareTo(decision.clearance()) > 0;
        return broader ? emergency : decision;
    }

    private AccessDecision emergency(AccessRequest request, ActionRule rule, PatientSettings settings, AccessFacts facts, AccessDecision fallback) {
        Optional<Role> professional = request.subject().isSelf(request.patientId())
                ? Optional.empty()
                : request.subject().roles().stream().filter(Role::isProfessional).findFirst();
        if (professional.isEmpty()) {
            return fallback;
        }
        Role role = professional.get();
        if (!rule.emergency()) {
            return AccessDecision.deny(ReasonCode.EMERGENCY_NOT_FOR_ACTION, role);
        }
        if (request.emergencyJustification().strip().length() < MIN_JUSTIFICATION_LENGTH) {
            return AccessDecision.deny(ReasonCode.EMERGENCY_JUSTIFICATION_MISSING, role);
        }
        if (!settings.consentActive() || settings.emergencyMode() == EmergencyMode.DISABLED
                || facts.isExcluded(request.subject().personId(), request.patientId())) {
            return AccessDecision.deny(ReasonCode.EMERGENCY_UNAVAILABLE, role);
        }
        return AccessDecision.emergency(role, settings.emergencyMode() == EmergencyMode.NORMAL_AND_RESTRICTED
                ? Confidentiality.RESTREINT : Confidentiality.NORMAL);
    }
}
