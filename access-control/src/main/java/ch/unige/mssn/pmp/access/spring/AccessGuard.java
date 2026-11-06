package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.audit.AuditEvent;
import ch.unige.mssn.pmp.access.audit.AuditLog;
import ch.unige.mssn.pmp.access.engine.AccessDecisionEngine;
import ch.unige.mssn.pmp.access.engine.AccessFacts;
import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Outcome;
import ch.unige.mssn.pmp.access.model.ReasonCode;
import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import ch.unige.mssn.pmp.access.policy.ActionRule;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Enchaîne décision et journal. Une erreur vaut refus, et rien ne s'exécute sans trace écrite. */
public class AccessGuard {

    private static final Logger LOG = LoggerFactory.getLogger(AccessGuard.class);
    private static final int MAX_FREE_TEXT = 500;
    private static final String UNIDENTIFIED = "inconnu";

    private final AccessDecisionEngine engine;
    private final AccessFacts facts;
    private final AccessPolicy policy;
    private final AuditLog auditLog;

    public AccessGuard(AccessDecisionEngine engine, AccessFacts facts, AccessPolicy policy, AuditLog auditLog) {
        this.engine = engine;
        this.facts = facts;
        this.policy = policy;
        this.auditLog = auditLog;
    }

    public AccessDecision check(AccessRequest request) {
        AccessDecision decision = decideSafely(request);
        UUID eventUuid = UUID.randomUUID();
        record(eventUuid, request, decision);
        if (!decision.permitted()) {
            throw new AccessRefusedException(decision, eventUuid);
        }
        return decision;
    }

    /** Refuse et trace une demande qu'on n'a pas pu formuler, par exemple une expression SpEL en erreur. */
    public AccessRefusedException refuseUndecidable(AccessRequest request) {
        AccessDecision decision = AccessDecision.deny(ReasonCode.DECISION_ERROR, null);
        UUID eventUuid = UUID.randomUUID();
        record(eventUuid, request, decision);
        return new AccessRefusedException(decision, eventUuid);
    }

    private AccessDecision decideSafely(AccessRequest request) {
        try {
            return engine.decide(request, facts);
        } catch (RuntimeException e) {
            LOG.error("Décision impossible, refus par défaut", e);
            return AccessDecision.deny(ReasonCode.DECISION_ERROR, null);
        }
    }

    private void record(UUID eventUuid, AccessRequest request, AccessDecision decision) {
        try {
            auditLog.append(toEvent(eventUuid, request, decision));
        } catch (RuntimeException e) {
            LOG.error("Journal indisponible, demande bloquée", e);
            throw new AuditUnavailableException(e);
        }
    }

    private AuditEvent toEvent(UUID eventUuid, AccessRequest request, AccessDecision decision) {
        var subject = request.subject();
        Role role = decision.effectiveRole();
        boolean emergency = decision.outcome() == Outcome.EMERGENCY_PERMIT;
        String atcEvent = policy.rule(request.resource(), request.action()).map(ActionRule::atcEvent).orElse(null);
        return new AuditEvent(eventUuid, request.at().truncatedTo(ChronoUnit.MICROS),
                subject.personId() == null ? UNIDENTIFIED : subject.personId(),
                clean(subject.displayName()), role == null ? null : role.name(), request.patientId(),
                request.resource(), request.action(), atcEvent, decision.outcome().name(), decision.reason().name(),
                decision.reason().journalText(), request.emergencyRequested() ? "EMER" : "NORM",
                clean(request.emergencyJustification()), clean(request.reference()), policy.version());
    }

    /** Retire les caractères de contrôle d'un texte libre, pour qu'il ne puisse pas imiter une ligne du journal. */
    static String clean(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.replaceAll("\\p{Cntrl}", " ").strip();
        return cleaned.length() <= MAX_FREE_TEXT ? cleaned : cleaned.substring(0, MAX_FREE_TEXT);
    }
}
