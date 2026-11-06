package ch.unige.mssn.pmp.access;

import static org.assertj.core.api.Assertions.assertThat;

import ch.unige.mssn.pmp.access.engine.AccessDecisionEngine;
import ch.unige.mssn.pmp.access.engine.AccessFacts.EmergencyMode;
import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Outcome;
import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.model.Subject;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tableau attendu saisi à la main d'après la proposition, sans lecture du fichier de règles : si le fichier YAML
 * s'écarte du tableau, ce test échoue. Chaque ligne vérifie tous les rôles, avec et sans lien avec le patient.
 */
class RuleMatrixTest {

    private static final String PATIENT = "P1";
    private static final String OTHER_PATIENT = "P2";
    private static final List<Role> COLUMNS = List.of(Role.PATIENT, Role.REPRESENTANT, Role.MEDECIN, Role.PHARMACIEN, Role.SOIGNANT, Role.AUDITEUR);

    private final AccessDecisionEngine engine = new AccessDecisionEngine(
            AccessPolicy.load(RuleMatrixTest.class.getResourceAsStream("/access-policy.yaml")));

    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource(textBlock = """
            # ressource,        action,                      patient, repr., médecin, pharm., soignant, auditeur
            PLAN_MEDICATION,    CONSULTER_PLAN,              O,       O,     O,       O,      O,        N
            PLAN_MEDICATION,    INSCRIRE_TRAITEMENT,         N,       N,     O,       N,      N,        N
            PLAN_MEDICATION,    PRESCRIRE,                   N,       N,     O,       N,      N,        N
            PLAN_MEDICATION,    ENREGISTRER_DELIVRANCE,      N,       N,     N,       O,      N,        N
            PLAN_MEDICATION,    MODIFIER_TRAITEMENT,         N,       N,     O,       N,      N,        N
            PLAN_MEDICATION,    EMETTRE_AVIS_PHARMACEUTIQUE, N,       N,     N,       O,      N,        N
            JOURNAL_ACCES,      CONSULTER_JOURNAL,           O,       O,     N,       N,      N,        O
            """)
    void withRelationship(String resource, String action, String patient, String representative, String physician,
                          String pharmacist, String caregiver, String auditor) {
        List<String> expected = List.of(patient, representative, physician, pharmacist, caregiver, auditor);
        for (int i = 0; i < COLUMNS.size(); i++) {
            Role role = COLUMNS.get(i);
            AccessDecision decision = engine.decide(request(role, PATIENT, resource, action, null), facts(action));
            assertThat(decision.permitted())
                    .as("%s / %s avec lien", role, action)
                    .isEqualTo(expected.get(i).equals("O"));
        }
    }

    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource({
            "PLAN_MEDICATION, CONSULTER_PLAN", "PLAN_MEDICATION, INSCRIRE_TRAITEMENT", "PLAN_MEDICATION, PRESCRIRE",
            "PLAN_MEDICATION, ENREGISTRER_DELIVRANCE", "PLAN_MEDICATION, MODIFIER_TRAITEMENT",
            "PLAN_MEDICATION, EMETTRE_AVIS_PHARMACEUTIQUE", "JOURNAL_ACCES, CONSULTER_JOURNAL"})
    void withoutRelationshipOnlyOversightReadsTheJournal(String resource, String action) {
        for (Role role : COLUMNS) {
            AccessDecision decision = engine.decide(request(role, OTHER_PATIENT, resource, action, null), facts(action));
            boolean expected = role == Role.AUDITEUR && action.equals("CONSULTER_JOURNAL");
            assertThat(decision.permitted()).as("%s / %s sans lien", role, action).isEqualTo(expected);
        }
    }

    /** Le bris de glace ouvre la consultation aux soignants, et rien d'autre à personne. */
    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource(textBlock = """
            # ressource,        action,                      patient, repr., médecin, pharm., soignant, auditeur
            PLAN_MEDICATION,    CONSULTER_PLAN,              N,       N,     U,       U,      U,        N
            PLAN_MEDICATION,    INSCRIRE_TRAITEMENT,         N,       N,     N,       N,      N,        N
            PLAN_MEDICATION,    PRESCRIRE,                   N,       N,     N,       N,      N,        N
            PLAN_MEDICATION,    ENREGISTRER_DELIVRANCE,      N,       N,     N,       N,      N,        N
            PLAN_MEDICATION,    MODIFIER_TRAITEMENT,         N,       N,     N,       N,      N,        N
            PLAN_MEDICATION,    EMETTRE_AVIS_PHARMACEUTIQUE, N,       N,     N,       N,      N,        N
            JOURNAL_ACCES,      CONSULTER_JOURNAL,           N,       N,     N,       N,      N,        O
            """)
    void emergencyWithoutRelationship(String resource, String action, String patient, String representative, String physician,
                                      String pharmacist, String caregiver, String auditor) {
        List<String> expected = List.of(patient, representative, physician, pharmacist, caregiver, auditor);
        for (int i = 0; i < COLUMNS.size(); i++) {
            Role role = COLUMNS.get(i);
            AccessDecision decision = engine.decide(
                    request(role, OTHER_PATIENT, resource, action, "Patient inconscient aux urgences"), facts(action));
            Outcome outcome = switch (expected.get(i)) {
                case "U" -> Outcome.EMERGENCY_PERMIT;
                case "O" -> Outcome.PERMIT;
                default -> Outcome.DENY;
            };
            assertThat(decision.outcome()).as("%s / %s en urgence", role, action).isEqualTo(outcome);
        }
    }

    private static AccessRequest request(Role role, String patient, String resource, String action, String justification) {
        Subject subject = new Subject("person", role == Role.PATIENT ? PATIENT : null, "Personne", Set.of(role));
        return new AccessRequest(subject, patient, resource, action, Confidentiality.NORMAL, justification, null, FakeFacts.NOW);
    }

    private static FakeFacts facts(String action) {
        return new FakeFacts()
                .patient(PATIENT)
                .patient(OTHER_PATIENT, true, EmergencyMode.NORMAL_ONLY)
                .grant("person", PATIENT, Confidentiality.NORMAL)
                .mandate("person", PATIENT, FakeFacts.PAST, null, false, Set.of(action));
    }
}
