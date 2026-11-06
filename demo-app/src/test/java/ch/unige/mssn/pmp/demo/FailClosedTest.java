package ch.unige.mssn.pmp.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import ch.unige.mssn.pmp.access.audit.AuditLog;
import ch.unige.mssn.pmp.access.engine.AccessFacts;
import ch.unige.mssn.pmp.demo.support.ApiClient;
import ch.unige.mssn.pmp.demo.support.IntegrationEnvironment;
import ch.unige.mssn.pmp.demo.support.Personas;
import ch.unige.mssn.pmp.demo.support.TestTokens;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/** En cas de panne, le module refuse : une action sans contrôle ou sans trace n'a jamais lieu. */
@SpringBootTest(classes = PlanMedicationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FailClosedTest {

    private static final String PLAN = "/api/patients/" + Personas.DUPONT + "/plan";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        IntegrationEnvironment.register(registry);
    }

    @Autowired
    private Environment environment;
    @Autowired
    private JdbcClient jdbc;
    @MockitoSpyBean
    private AuditLog auditLog;
    @MockitoSpyBean
    private AccessFacts facts;

    private ApiClient api;
    private String martin;

    @BeforeEach
    void client() {
        api = new ApiClient(environment.getRequiredProperty("local.server.port", Integer.class));
        martin = TestTokens.forPersona(Personas.named("Dre Martin"));
    }

    @AfterEach
    void restore() {
        reset(auditLog, facts);
    }

    private long treatments() {
        return jdbc.sql("SELECT count(*) FROM plan.treatment").query(Long.class).single();
    }

    @Test
    void journalFailureBlocksAReadAndAWrite() {
        doThrow(new IllegalStateException("base du journal hors service")).when(auditLog).append(any());
        long before = treatments();

        var read = api.get(martin, PLAN);
        var write = api.post(martin, PLAN + "/traitements", ApiClient.json(Map.of("atc", "A", "medication", "Sans trace", "dosage", "x")));

        assertThat(read.status()).isEqualTo(503);
        assertThat(read.body()).doesNotContain("Ramipril", "hors service");
        assertThat(write.status()).isEqualTo(503);
        assertThat(treatments()).isEqualTo(before);
    }

    @Test
    void journalFailureAlsoBlocksEmergencyAccess() {
        doThrow(new IllegalStateException("base du journal hors service")).when(auditLog).append(any());
        var answer = api.post(TestTokens.forPersona(Personas.named("Dr Keller")), PLAN + "/acces-urgence",
                ApiClient.json(Map.of("justification", "Patiente inconsciente aux urgences")));
        assertThat(answer.status()).isEqualTo(503);
    }

    @Test
    void factsFailureIsARefusalAndIsTraced() {
        when(facts.patientSettings(anyString())).thenThrow(new IllegalStateException("droits indisponibles"));
        var answer = api.get(martin, PLAN);
        assertThat(answer.status()).isEqualTo(503);
        assertThat(answer.body()).doesNotContain("indisponibles", "Ramipril");
        String reason = jdbc.sql("SELECT reason_code FROM audit.audit_event WHERE person_id = ? ORDER BY seq DESC LIMIT 1")
                .param(Personas.named("Dre Martin").personId()).query(String.class).single();
        assertThat(reason).isEqualTo("DECISION_ERROR");
    }

    @Test
    void refusedRequestIsTracedEvenThoughNothingElseHappens() {
        long seqBefore = jdbc.sql("SELECT last_seq FROM audit.chain_head").query(Long.class).single();
        api.get(TestTokens.forPersona(Personas.named("le soignant Blanc")), PLAN);
        long seqAfter = jdbc.sql("SELECT last_seq FROM audit.chain_head").query(Long.class).single();
        assertThat(seqAfter).isEqualTo(seqBefore + 1);
    }
}
