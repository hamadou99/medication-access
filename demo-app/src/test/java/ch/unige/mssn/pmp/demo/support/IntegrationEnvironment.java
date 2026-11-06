package ch.unige.mssn.pmp.demo.support;

import org.springframework.test.context.DynamicPropertyRegistry;

public final class IntegrationEnvironment {

    public static final String AUDIT_KEY = "cle-de-test-du-journal-0123456789abcdef";

    private IntegrationEnvironment() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestDatabase::jdbcUrl);
        registry.add("spring.datasource.username", () -> TestDatabase.APP);
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.flyway.url", TestDatabase::jdbcUrl);
        registry.add("spring.flyway.user", () -> TestDatabase.OWNER);
        registry.add("spring.flyway.password", () -> "test");
        registry.add("pmp.access.audit-key", () -> AUDIT_KEY);
        registry.add("pmp.security.issuer", () -> TestTokens.ISSUER);
        registry.add("pmp.security.jwk-set-uri", TestTokens::jwksUri);
    }
}
