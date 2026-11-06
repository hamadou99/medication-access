package ch.unige.mssn.pmp.demo;

import static org.assertj.core.api.Assertions.assertThat;

import ch.unige.mssn.pmp.access.spring.AccessControlled;
import ch.unige.mssn.pmp.demo.support.ApiClient;
import ch.unige.mssn.pmp.demo.support.IntegrationEnvironment;
import ch.unige.mssn.pmp.demo.support.Personas;
import ch.unige.mssn.pmp.demo.support.TestTokens;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Frontière : aucun appel sans jeton valide, et aucun point d'entrée sans contrôle d'accès. */
@SpringBootTest(classes = PlanMedicationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityBoundaryTest {

    private static final String PLAN = "/api/patients/" + Personas.DUPONT + "/plan";
    private static final Personas.Persona MARTIN = Personas.named("Dre Martin");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        IntegrationEnvironment.register(registry);
    }

    @Autowired
    private Environment environment;
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;
    @Autowired
    private JdbcClient jdbc;

    private ApiClient api;

    @BeforeEach
    void client() {
        api = new ApiClient(environment.getRequiredProperty("local.server.port", Integer.class));
    }

    @Test
    void validTokenIsAccepted() {
        assertThat(api.get(TestTokens.forPersona(MARTIN), PLAN).status()).isEqualTo(200);
    }

    @Test
    void noTokenIsRejected() {
        assertThat(api.get(null, PLAN).status()).isEqualTo(401);
    }

    @Test
    void garbageTokenIsRejected() {
        assertThat(api.get("pas-un-jeton", PLAN).status()).isEqualTo(401);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = TestTokens.modified(MARTIN, claims -> claims.expirationTime(Date.from(Instant.now().minusSeconds(3600))));
        assertThat(api.get(token, PLAN).status()).isEqualTo(401);
    }

    @Test
    void wrongIssuerIsRejected() {
        String token = TestTokens.modified(MARTIN, claims -> claims.issuer("http://autre.invalid/realms/pmp"));
        assertThat(api.get(token, PLAN).status()).isEqualTo(401);
    }

    @Test
    void wrongAudienceIsRejected() {
        String token = TestTokens.modified(MARTIN, claims -> claims.audience("autre-api"));
        assertThat(api.get(token, PLAN).status()).isEqualTo(401);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        assertThat(api.get(TestTokens.signedWithForeignKey(MARTIN), PLAN).status()).isEqualTo(401);
    }

    @Test
    void unsignedTokenIsRejected() {
        String signed = TestTokens.forPersona(MARTIN);
        String unsigned = "eyJhbGciOiJub25lIn0." + signed.split("\\.")[1] + ".";
        assertThat(api.get(unsigned, PLAN).status()).isEqualTo(401);
    }

    @Test
    void tokenWithoutPersonIdentifierIsDeniedAndTraced() {
        String token = TestTokens.modified(MARTIN, claims -> claims.claim("pmp_person_id", null));
        assertThat(api.get(token, PLAN).status()).isEqualTo(403);
        assertThat(lastOutcomeFor("inconnu")).isEqualTo("DENY");
    }

    @Test
    void unknownRoleGrantsNothing() {
        String token = TestTokens.modified(MARTIN, claims -> claims.claim("realm_access", Map.of("roles", List.of("ADMIN", "medecin"))));
        assertThat(api.get(token, PLAN).status()).isEqualTo(403);
    }

    @Test
    void forgedPatientClaimGrantsAccessOnlyToThatRole() {
        // Un jeton valide dit qui est la personne : il faut qu'il porte aussi le rôle PATIENT pour agir comme patient.
        String token = TestTokens.modified(MARTIN, claims -> claims.claim("epr_spid", Personas.MOREL));
        assertThat(api.get(token, "/api/patients/" + Personas.MOREL + "/plan").status()).isEqualTo(403);
    }

    @Test
    void sqlInjectionInThePatientIdentifierIsHarmless() {
        var answer = api.get(TestTokens.forPersona(MARTIN), "/api/patients/x'%20OR%20'1'='1/plan");
        assertThat(answer.status()).isIn(400, 403);
    }

    @Test
    void refusalDoesNotLeakInternalDetails() {
        var answer = api.get(TestTokens.forPersona(Personas.named("le soignant Blanc")), PLAN);
        assertThat(answer.status()).isEqualTo(403);
        assertThat(answer.body()).doesNotContain("NO_RELATIONSHIP", "Exception", "SELECT", "access_grant");
        assertThat(answer.json().get("reference").asString()).hasSize(36);
    }

    @Test
    void everyEndpointDeclaresItsAccessControl() {
        var unprotected = mappings.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getValue().getBeanType().getPackageName().startsWith("ch.unige.mssn.pmp"))
                .filter(entry -> !entry.getValue().hasMethodAnnotation(AccessControlled.class))
                .map(entry -> entry.getKey().toString())
                .toList();
        assertThat(unprotected).isEmpty();
        assertThat(mappings.getHandlerMethods().keySet().stream()
                .filter(info -> info.toString().contains("/api/")).count()).isGreaterThanOrEqualTo(10);
    }

    private String lastOutcomeFor(String person) {
        return jdbc.sql("SELECT outcome FROM audit.audit_event WHERE person_id = ? ORDER BY seq DESC LIMIT 1")
                .param(person).query(String.class).single();
    }
}
