package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.audit.AuditChainVerifier;
import ch.unige.mssn.pmp.access.audit.AuditHasher;
import ch.unige.mssn.pmp.access.audit.AuditLog;
import ch.unige.mssn.pmp.access.audit.AuditQueries;
import ch.unige.mssn.pmp.access.engine.AccessDecisionEngine;
import ch.unige.mssn.pmp.access.engine.AccessFacts;
import ch.unige.mssn.pmp.access.policy.AccessPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

/** Il suffit d'ajouter le module à l'application et de fournir la clé du journal. */
@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration")
@EnableConfigurationProperties(AccessControlProperties.class)
@Import(AccessProblemHandler.class)
public class AccessControlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Clock accessClock() {
        return Clock.systemUTC();
    }

    @Bean
    AccessPolicy accessPolicy(AccessControlProperties properties, ResourceLoader loader) {
        InputStream[] sources = properties.policyLocations().stream().map(location -> open(loader, location)).toArray(InputStream[]::new);
        return AccessPolicy.load(sources);
    }

    @Bean
    AccessDecisionEngine accessDecisionEngine(AccessPolicy policy) {
        return new AccessDecisionEngine(policy);
    }

    @Bean
    @ConditionalOnMissingBean
    AccessFacts accessFacts(JdbcClient jdbc) {
        return new JdbcAccessFacts(jdbc);
    }

    @Bean
    AuditHasher auditHasher(AccessControlProperties properties) {
        if (properties.auditKey() == null) {
            throw new IllegalStateException("pmp.access.audit-key est obligatoire");
        }
        return new AuditHasher(properties.auditKey().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    AuditLog auditLog(JdbcClient jdbc, PlatformTransactionManager transactions, AuditHasher hasher) {
        return new AuditLog(jdbc, transactions, hasher);
    }

    @Bean
    AuditQueries auditQueries(JdbcClient jdbc) {
        return new AuditQueries(jdbc);
    }

    @Bean
    AuditChainVerifier auditChainVerifier(JdbcTemplate jdbc, AuditHasher hasher) {
        return new AuditChainVerifier(jdbc, hasher);
    }

    @Bean
    AccessGuard accessGuard(AccessDecisionEngine engine, AccessFacts facts, AccessPolicy policy, AuditLog auditLog) {
        return new AccessGuard(engine, facts, policy, auditLog);
    }

    @Bean
    SubjectResolver subjectResolver() {
        return new SubjectResolver();
    }

    @Bean
    AccessControlAspect accessControlAspect(AccessGuard guard, SubjectResolver subjects, BeanFactory beans, Clock clock) {
        return new AccessControlAspect(guard, subjects, beans, clock);
    }

    private static InputStream open(ResourceLoader loader, String location) {
        try {
            return loader.getResource(location).getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("Tableau de règles introuvable : " + location, e);
        }
    }
}
