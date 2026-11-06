package ch.unige.mssn.pmp.access;

import static ch.unige.mssn.pmp.access.FakeFacts.FUTURE;
import static ch.unige.mssn.pmp.access.FakeFacts.NOW;
import static ch.unige.mssn.pmp.access.FakeFacts.PAST;
import static org.assertj.core.api.Assertions.assertThat;

import ch.unige.mssn.pmp.access.engine.AccessDecisionEngine;
import ch.unige.mssn.pmp.access.engine.AccessFacts.EmergencyMode;
import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Outcome;
import ch.unige.mssn.pmp.access.model.ReasonCode;
import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.model.Subject;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AccessDecisionEngineTest {

    private static final String DUPONT = "P-DUPONT";
    private static final String PLAN = "PLAN_MEDICATION";
    private static final String CONSULT = "CONSULTER_PLAN";
    private static final String MODIFY = "MODIFIER_TRAITEMENT";
    private static final String WHY = "Patient inconscient aux urgences";

    private final AccessDecisionEngine engine = new AccessDecisionEngine(
            AccessPolicy.load(AccessDecisionEngineTest.class.getResourceAsStream("/access-policy.yaml")));

    private static Subject person(String id, Role... roles) {
        return new Subject(id, null, id, Set.of(roles));
    }

    private AccessDecision decide(Subject subject, String action, Confidentiality level, String justification, FakeFacts facts) {
        return engine.decide(new AccessRequest(subject, DUPONT, PLAN, action, level, justification, null, NOW), facts);
    }

    private AccessDecision consult(Subject subject, FakeFacts facts) {
        return decide(subject, CONSULT, null, null, facts);
    }

    private AccessDecision consultInEmergency(Subject subject, String justification, FakeFacts facts) {
        return decide(subject, CONSULT, null, justification, facts);
    }

    private static FakeFacts dupont() {
        return new FakeFacts().patient(DUPONT);
    }

    @Test
    void actionAbsentFromThePolicyIsDenied() {
        AccessDecision decision = engine.decide(new AccessRequest(person("a", Role.MEDECIN), DUPONT, PLAN, "SUPPRIMER_TOUT", null, null, null, NOW), dupont());
        assertThat(decision.reason()).isEqualTo(ReasonCode.UNKNOWN_ACTION);
    }

    @Test
    void tokenWithoutIdentityOrRoleIsDenied() {
        assertThat(consult(new Subject(null, null, null, Set.of(Role.MEDECIN)), dupont()).reason()).isEqualTo(ReasonCode.INVALID_SUBJECT);
        assertThat(consult(new Subject("a", null, null, Set.of()), dupont()).reason()).isEqualTo(ReasonCode.INVALID_SUBJECT);
    }

    @Test
    void unknownPatientAndMissingRelationshipLookIdenticalToTheRequester() {
        AccessDecision unknown = consult(person("a", Role.MEDECIN), new FakeFacts());
        AccessDecision noLink = consult(person("a", Role.MEDECIN), dupont());
        assertThat(unknown.reason()).isEqualTo(ReasonCode.PATIENT_UNKNOWN);
        assertThat(noLink.reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
        assertThat(unknown.reason().publicMessage()).isEqualTo(noLink.reason().publicMessage());
    }

    @Test
    void missingPatientIdentifierIsDenied() {
        var request = new AccessRequest(person("a", Role.MEDECIN), null, PLAN, CONSULT, null, null, null, NOW);
        assertThat(engine.decide(request, dupont()).reason()).isEqualTo(ReasonCode.PATIENT_UNKNOWN);
    }

    @Test
    void relationshipIsCheckedBeforeRole() {
        assertThat(decide(person("a", Role.PHARMACIEN), "PRESCRIRE", null, null, dupont()).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
    }

    @Test
    void roleIsCheckedBeforeValidity() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL, PAST, PAST.plusSeconds(1));
        assertThat(decide(person("a", Role.PHARMACIEN), "PRESCRIRE", null, null, facts).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void grantValidityBoundaries() {
        Subject doctor = person("a", Role.MEDECIN);
        assertThat(consult(doctor, dupont().grant("a", DUPONT, Confidentiality.NORMAL, NOW, null)).permitted()).isTrue();
        assertThat(consult(doctor, dupont().grant("a", DUPONT, Confidentiality.NORMAL, PAST, NOW)).reason()).isEqualTo(ReasonCode.GRANT_EXPIRED);
        assertThat(consult(doctor, dupont().grant("a", DUPONT, Confidentiality.NORMAL, FUTURE, null)).reason()).isEqualTo(ReasonCode.GRANT_EXPIRED);
        assertThat(consult(doctor, dupont().grant("a", DUPONT, Confidentiality.NORMAL, PAST, FUTURE)).permitted()).isTrue();
    }

    @Test
    void permitCarriesTheLevelGranted() {
        AccessDecision decision = consult(person("a", Role.MEDECIN), dupont().grant("a", DUPONT, Confidentiality.RESTREINT));
        assertThat(decision.outcome()).isEqualTo(Outcome.PERMIT);
        assertThat(decision.reason()).isEqualTo(ReasonCode.CARE_RELATIONSHIP);
        assertThat(decision.clearance()).isEqualTo(Confidentiality.RESTREINT);
        assertThat(decision.effectiveRole()).isEqualTo(Role.MEDECIN);
    }

    @Test
    void dataAboveTheGrantedLevelCannotBeModified() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL);
        assertThat(decide(person("a", Role.MEDECIN), MODIFY, Confidentiality.RESTREINT, null, facts).reason()).isEqualTo(ReasonCode.CONFIDENTIALITY_TOO_HIGH);
        assertThat(decide(person("a", Role.MEDECIN), MODIFY, Confidentiality.NORMAL, null, facts).permitted()).isTrue();
    }

    @Test
    void secretDataStaysOutOfReachOfProfessionalsEvenWithTheHighestGrant() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.RESTREINT);
        assertThat(decide(person("a", Role.MEDECIN), MODIFY, Confidentiality.SECRET, null, facts).reason()).isEqualTo(ReasonCode.CONFIDENTIALITY_TOO_HIGH);
        assertThat(consult(person("a", Role.MEDECIN), facts).clearance()).isEqualTo(Confidentiality.RESTREINT);
    }

    @Test
    void patientExclusionWinsOverAnExistingGrant() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL).exclude("a", DUPONT);
        assertThat(consult(person("a", Role.MEDECIN), facts).reason()).isEqualTo(ReasonCode.EXCLUDED_BY_PATIENT);
    }

    @Test
    void exclusionAlsoClosesTheEmergencyAccess() {
        FakeFacts facts = dupont().exclude("a", DUPONT);
        assertThat(consultInEmergency(person("a", Role.MEDECIN), WHY, facts).reason()).isEqualTo(ReasonCode.EMERGENCY_UNAVAILABLE);
    }

    @Test
    void revokedConsentClosesEveryThirdParty() {
        FakeFacts facts = new FakeFacts().patient(DUPONT, false, EmergencyMode.NORMAL_ONLY)
                .grant("a", DUPONT, Confidentiality.NORMAL)
                .mandate("r", DUPONT, PAST, null, false, Set.of(CONSULT));
        assertThat(consult(person("a", Role.MEDECIN), facts).reason()).isEqualTo(ReasonCode.CONSENT_REVOKED);
        assertThat(consult(person("r", Role.REPRESENTANT), facts).reason()).isEqualTo(ReasonCode.CONSENT_REVOKED);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), WHY, facts).reason()).isEqualTo(ReasonCode.EMERGENCY_UNAVAILABLE);
    }

    @Test
    void patientKeepsAccessAfterRevokingConsent() {
        Subject patient = new Subject("p", DUPONT, "Mme Dupont", Set.of(Role.PATIENT));
        AccessDecision decision = consult(patient, new FakeFacts().patient(DUPONT, false, EmergencyMode.NORMAL_ONLY));
        assertThat(decision.permitted()).isTrue();
        assertThat(decision.reason()).isEqualTo(ReasonCode.SELF_ACCESS);
        assertThat(decision.clearance()).isEqualTo(Confidentiality.SECRET);
    }

    @Test
    void patientRoleGivesNothingOnSomeoneElsesPlan() {
        Subject other = new Subject("p", "P-AUTRE", "Autre", Set.of(Role.PATIENT));
        assertThat(consult(other, dupont()).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
    }

    @Test
    void doctorActsAsPatientOnOwnPlan() {
        Subject doctorAndPatient = new Subject("g", DUPONT, "Dr", Set.of(Role.MEDECIN, Role.PATIENT));
        FakeFacts facts = dupont().grant("g", DUPONT, Confidentiality.RESTREINT);
        assertThat(consult(doctorAndPatient, facts).effectiveRole()).isEqualTo(Role.PATIENT);
        assertThat(decide(doctorAndPatient, "PRESCRIRE", null, null, facts).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void ownPlanWithoutPatientRoleIsDeniedAndEmergencyCannotBypassIt() {
        Subject doctorOnly = new Subject("g", DUPONT, "Dr", Set.of(Role.MEDECIN));
        FakeFacts facts = dupont().grant("g", DUPONT, Confidentiality.RESTREINT);
        assertThat(consult(doctorOnly, facts).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
        assertThat(consultInEmergency(doctorOnly, WHY, facts).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
    }

    @Test
    void mandateChecks() {
        Subject rep = person("r", Role.REPRESENTANT);
        assertThat(consult(rep, dupont()).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
        assertThat(consult(rep, dupont().mandate("r", DUPONT, PAST, NOW, false, Set.of(CONSULT))).reason()).isEqualTo(ReasonCode.MANDATE_INVALID);
        assertThat(consult(rep, dupont().mandate("r", DUPONT, FUTURE, null, false, Set.of(CONSULT))).reason()).isEqualTo(ReasonCode.MANDATE_INVALID);
        assertThat(consult(rep, dupont().mandate("r", DUPONT, PAST, null, true, Set.of(CONSULT))).reason()).isEqualTo(ReasonCode.MANDATE_INVALID);
        assertThat(consult(rep, dupont().mandate("r", DUPONT, PAST, null, false, Set.of("CONSULTER_JOURNAL"))).reason()).isEqualTo(ReasonCode.MANDATE_SCOPE);
        AccessDecision valid = consult(rep, dupont().mandate("r", DUPONT, NOW, FUTURE, false, Set.of(CONSULT)));
        assertThat(valid.reason()).isEqualTo(ReasonCode.VALID_MANDATE);
        assertThat(valid.clearance()).isEqualTo(Confidentiality.SECRET);
    }

    @Test
    void representativeCannotWriteWhateverTheMandateSays() {
        FakeFacts facts = dupont().mandate("r", DUPONT, PAST, null, false, Set.of(MODIFY));
        assertThat(decide(person("r", Role.REPRESENTANT), MODIFY, null, null, facts).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void emergencyGivesNormalLevelByDefault() {
        AccessDecision decision = consultInEmergency(person("u", Role.MEDECIN), WHY, dupont());
        assertThat(decision.outcome()).isEqualTo(Outcome.EMERGENCY_PERMIT);
        assertThat(decision.reason()).isEqualTo(ReasonCode.EMERGENCY_ACCESS);
        assertThat(decision.clearance()).isEqualTo(Confidentiality.NORMAL);
        assertThat(decision.effectiveRole()).isEqualTo(Role.MEDECIN);
    }

    @Test
    void emergencyExtendsToRestrictedWhenThePatientChoseIt() {
        FakeFacts facts = new FakeFacts().patient(DUPONT, true, EmergencyMode.NORMAL_AND_RESTRICTED);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), WHY, facts).clearance()).isEqualTo(Confidentiality.RESTREINT);
    }

    @Test
    void emergencyClosedByThePatient() {
        FakeFacts facts = new FakeFacts().patient(DUPONT, true, EmergencyMode.DISABLED);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), WHY, facts).reason()).isEqualTo(ReasonCode.EMERGENCY_UNAVAILABLE);
    }

    @Test
    void emergencyRequiresAJustification() {
        assertThat(consultInEmergency(person("u", Role.MEDECIN), "", dupont()).reason()).isEqualTo(ReasonCode.EMERGENCY_JUSTIFICATION_MISSING);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), "      urgent    ", dupont()).reason()).isEqualTo(ReasonCode.EMERGENCY_JUSTIFICATION_MISSING);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), "123456789", dupont()).reason()).isEqualTo(ReasonCode.EMERGENCY_JUSTIFICATION_MISSING);
        assertThat(consultInEmergency(person("u", Role.MEDECIN), "1234567890", dupont()).outcome()).isEqualTo(Outcome.EMERGENCY_PERMIT);
    }

    @Test
    void emergencyNeverOpensAWrite() {
        assertThat(decide(person("u", Role.MEDECIN), MODIFY, null, WHY, dupont()).reason()).isEqualTo(ReasonCode.EMERGENCY_NOT_FOR_ACTION);
    }

    @Test
    void emergencyDoesNotGiveARoleAnActionItLacks() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL);
        assertThat(decide(person("a", Role.PHARMACIEN), "PRESCRIRE", null, WHY, facts).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void emergencyDoesNotOverrideDataLevelOnAWrite() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL);
        assertThat(decide(person("a", Role.MEDECIN), MODIFY, Confidentiality.RESTREINT, WHY, facts).reason()).isEqualTo(ReasonCode.CONFIDENTIALITY_TOO_HIGH);
    }

    @Test
    void emergencyRepairsAnExpiredGrant() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL, PAST, PAST.plusSeconds(1));
        assertThat(consultInEmergency(person("a", Role.MEDECIN), WHY, facts).outcome()).isEqualTo(Outcome.EMERGENCY_PERMIT);
    }

    @Test
    void emergencyIsIgnoredWhenTheOrdinaryRightSuffices() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.RESTREINT);
        assertThat(consultInEmergency(person("a", Role.MEDECIN), WHY, facts).outcome()).isEqualTo(Outcome.PERMIT);
    }

    @Test
    void equalEmergencyLevelDoesNotCountAsBroader() {
        FakeFacts facts = dupont().grant("a", DUPONT, Confidentiality.NORMAL);
        assertThat(consultInEmergency(person("a", Role.MEDECIN), WHY, facts).outcome()).isEqualTo(Outcome.PERMIT);
    }

    @Test
    void emergencyRaisesANormalGrantWhenThePatientExtendedIt() {
        FakeFacts facts = new FakeFacts().patient(DUPONT, true, EmergencyMode.NORMAL_AND_RESTRICTED).grant("a", DUPONT, Confidentiality.NORMAL);
        AccessDecision decision = consultInEmergency(person("a", Role.MEDECIN), WHY, facts);
        assertThat(decision.outcome()).isEqualTo(Outcome.EMERGENCY_PERMIT);
        assertThat(decision.clearance()).isEqualTo(Confidentiality.RESTREINT);
    }

    @Test
    void emergencyIsReservedToProfessionals() {
        assertThat(consultInEmergency(person("r", Role.REPRESENTANT), WHY, dupont()).reason()).isEqualTo(ReasonCode.NO_RELATIONSHIP);
        assertThat(consultInEmergency(person("x", Role.AUDITEUR), WHY, dupont()).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void personHoldingTwoRolesGetsTheOneThatApplies() {
        Subject both = person("a", Role.REPRESENTANT, Role.MEDECIN);
        AccessDecision decision = consult(both, dupont().grant("a", DUPONT, Confidentiality.NORMAL));
        assertThat(decision.effectiveRole()).isEqualTo(Role.MEDECIN);
    }

    @Test
    void globalActionOnlyForOversight() {
        var auditor = new AccessRequest(person("x", Role.AUDITEUR), null, "JOURNAL_ACCES", "CONTROLER_ACCES", null, null, null, NOW);
        var doctor = new AccessRequest(person("a", Role.MEDECIN), null, "JOURNAL_ACCES", "CONTROLER_ACCES", null, null, null, NOW);
        AccessDecision granted = engine.decide(auditor, dupont());
        assertThat(granted.reason()).isEqualTo(ReasonCode.OVERSIGHT_ROLE);
        assertThat(granted.effectiveRole()).isEqualTo(Role.AUDITEUR);
        assertThat(engine.decide(doctor, dupont()).reason()).isEqualTo(ReasonCode.ROLE_NOT_ALLOWED);
    }

    @Test
    void oversightSeesNoClinicalData() {
        var request = new AccessRequest(person("x", Role.AUDITEUR), DUPONT, "JOURNAL_ACCES", "CONSULTER_JOURNAL", null, null, null, NOW);
        AccessDecision decision = engine.decide(request, dupont());
        assertThat(decision.permitted()).isTrue();
        assertThat(decision.clearance()).isNull();
        assertThat(consult(person("x", Role.AUDITEUR), dupont()).permitted()).isFalse();
    }

    @Test
    void everyRefusalHasAMessageAndEveryReasonAJournalText() {
        for (ReasonCode reason : ReasonCode.values()) {
            assertThat(reason.journalText()).isNotBlank();
        }
        assertThat(AccessDecision.deny(ReasonCode.NO_RELATIONSHIP, null).reason().publicMessage()).isNotBlank();
        assertThat(ReasonCode.EXCLUDED_BY_PATIENT.publicMessage()).doesNotContain("exclu");
    }
}
