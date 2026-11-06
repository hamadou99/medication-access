package ch.unige.mssn.pmp.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.unige.mssn.pmp.access.audit.AuditChainVerifier;
import ch.unige.mssn.pmp.access.audit.AuditEvent;
import ch.unige.mssn.pmp.access.audit.AuditLog;
import ch.unige.mssn.pmp.demo.support.IntegrationEnvironment;
import ch.unige.mssn.pmp.demo.support.TestDatabase;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Le journal résiste-t-il à l'effacement, et l'altération se voit-elle si un administrateur contourne les protections ? */
@Isolated
@SpringBootTest(classes = PlanMedicationApplication.class)
class AuditIntegrityTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        IntegrationEnvironment.register(registry);
    }

    @Autowired
    private AuditLog log;
    @Autowired
    private AuditChainVerifier verifier;

    private static AuditEvent event(String person) {
        return new AuditEvent(UUID.randomUUID(), Instant.now().truncatedTo(ChronoUnit.MICROS), person, "Nom", "MEDECIN", "P1",
                "PLAN_MEDICATION", "CONSULTER_PLAN", "ATC_DOC_READ", "PERMIT", "CARE_RELATIONSHIP", "Relation de soins établie",
                "NORM", null, null, "test");
    }

    private static void run(String user, String sql) throws SQLException {
        try (Connection connection = TestDatabase.connect(user); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void asSuperuser(String... statements) throws SQLException {
        try (Connection connection = TestDatabase.connectAsSuperuser(); Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    /** Effectue une altération malveillante puis restaure l'état, trigger compris. */
    private static void asTamperer(String tamper, String restore, Runnable check) throws SQLException {
        asSuperuser("ALTER TABLE audit.audit_event DISABLE TRIGGER USER", tamper);
        try {
            check.run();
        } finally {
            asSuperuser(restore, "ALTER TABLE audit.audit_event ENABLE TRIGGER USER");
        }
    }

    @Test
    void applicationRoleCannotChangeTheJournal() {
        log.append(event("a"));
        assertThatThrownBy(() -> run(TestDatabase.APP, "UPDATE audit.audit_event SET outcome = 'DENY'")).isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> run(TestDatabase.APP, "DELETE FROM audit.audit_event")).isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> run(TestDatabase.APP, "TRUNCATE audit.audit_event")).isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> run(TestDatabase.APP, "DROP TABLE audit.audit_event")).isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> run(TestDatabase.APP, "ALTER TABLE audit.audit_event DISABLE TRIGGER USER")).isInstanceOf(SQLException.class);
    }

    @Test
    void applicationRoleCannotChangeRights() {
        assertThatThrownBy(() -> run(TestDatabase.APP, "INSERT INTO access.access_grant VALUES ('moi', '761337000000000001', 'RESTREINT', now(), NULL)"))
                .isInstanceOf(SQLException.class);
        assertThatThrownBy(() -> run(TestDatabase.APP, "DELETE FROM access.access_exclusion")).isInstanceOf(SQLException.class);
    }

    @Test
    void evenTheOwnerIsStoppedByTheAppendOnlyTrigger() {
        log.append(event("a"));
        assertThatThrownBy(() -> run(TestDatabase.OWNER, "UPDATE audit.audit_event SET outcome = 'DENY'"))
                .isInstanceOf(SQLException.class).hasMessageContaining("ajout seul");
        assertThatThrownBy(() -> run(TestDatabase.OWNER, "DELETE FROM audit.audit_event"))
                .isInstanceOf(SQLException.class).hasMessageContaining("ajout seul");
        assertThatThrownBy(() -> run(TestDatabase.OWNER, "TRUNCATE audit.audit_event CASCADE"))
                .isInstanceOf(SQLException.class).hasMessageContaining("ajout seul");
    }

    @Test
    void reviewsAreAppendOnlyToo() {
        assertThatThrownBy(() -> run(TestDatabase.APP, "DELETE FROM audit.emergency_review WHERE id = -1 OR true"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void intactChainVerifies() {
        log.append(event("a"));
        log.append(event("b"));
        assertThat(verifier.verify().valid()).isTrue();
    }

    @Test
    void administratorChangingAnEventIsDetected() throws SQLException {
        long seq = log.append(event("victime"));
        asTamperer("UPDATE audit.audit_event SET outcome = 'DENY' WHERE seq = " + seq,
                "UPDATE audit.audit_event SET outcome = 'PERMIT' WHERE seq = " + seq,
                () -> {
                    var result = verifier.verify();
                    assertThat(result.valid()).isFalse();
                    assertThat(result.brokenAt()).isEqualTo(seq);
                    assertThat(result.problem()).contains("modifié");
                });
        assertThat(verifier.verify().valid()).isTrue();
    }

    @Test
    void administratorDeletingAnEventIsDetected() throws SQLException {
        long first = log.append(event("a"));
        long deleted = log.append(event("supprime"));
        log.append(event("c"));
        asSuperuser("CREATE TABLE IF NOT EXISTS public.copie AS SELECT * FROM audit.audit_event WHERE false");
        asTamperer("WITH d AS (DELETE FROM audit.audit_event WHERE seq = " + deleted + " RETURNING *) INSERT INTO public.copie SELECT * FROM d",
                "INSERT INTO audit.audit_event SELECT * FROM public.copie; DELETE FROM public.copie",
                () -> {
                    var result = verifier.verify();
                    assertThat(result.valid()).isFalse();
                    assertThat(result.brokenAt()).isEqualTo(deleted);
                    assertThat(result.problem()).contains("manquant");
                });
        assertThat(first).isLessThan(deleted);
        assertThat(verifier.verify().valid()).isTrue();
    }

    /** Sans clé, un administrateur ne peut pas réécrire l'empreinte d'une ligne altérée. */
    @Test
    void administratorRecomputingAPlainHashIsDetected() throws SQLException {
        long seq = log.append(event("victime2"));
        asTamperer("UPDATE audit.audit_event SET person_id = 'autre', hash = encode(sha256(convert_to(prev_hash || 'autre', 'UTF8')), 'hex') WHERE seq = " + seq,
                "UPDATE audit.audit_event SET person_id = 'victime2', hash = (SELECT last_hash FROM audit.chain_head) WHERE seq = " + seq,
                () -> assertThat(verifier.verify().valid()).isFalse());
    }

    @Test
    void truncatingTheEndIsOnlyRevealedByAnExternalAnchor() throws SQLException {
        long kept = log.append(event("garde"));
        String keptHash = hashOf(kept);
        long removed = log.append(event("fin-supprimee"));
        String headBefore = hashOf(removed);
        var anchor = new AuditChainVerifier.Anchor(removed, headBefore);
        assertThat(verifier.verify(anchor).valid()).isTrue();

        asSuperuser("CREATE TABLE IF NOT EXISTS public.copie2 AS SELECT * FROM audit.audit_event WHERE false");
        asTamperer("WITH d AS (DELETE FROM audit.audit_event WHERE seq = " + removed + " RETURNING *) INSERT INTO public.copie2 SELECT * FROM d; "
                        + "UPDATE audit.chain_head SET last_seq = " + kept + ", last_hash = '" + keptHash + "'",
                "INSERT INTO audit.audit_event SELECT * FROM public.copie2; DELETE FROM public.copie2; "
                        + "UPDATE audit.chain_head SET last_seq = " + removed + ", last_hash = '" + headBefore + "'",
                () -> {
                    assertThat(verifier.verify().valid()).as("sans ancrage, la coupure ne se voit pas").isTrue();
                    var withAnchor = verifier.verify(anchor);
                    assertThat(withAnchor.valid()).isFalse();
                    assertThat(withAnchor.problem()).contains("ancrage");
                });
    }

    @Test
    void concurrentWritersKeepTheChainContinuous() throws Exception {
        long before = verifier.verify().checked();
        int threads = 8;
        int perThread = 50;
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                String person = "concurrent-" + t;
                tasks.add(() -> {
                    for (int i = 0; i < perThread; i++) {
                        log.append(event(person));
                    }
                    return null;
                });
            }
            for (Future<Void> future : pool.invokeAll(tasks)) {
                future.get();
            }
        }
        var result = verifier.verify();
        assertThat(result.valid()).isTrue();
        assertThat(result.checked()).isGreaterThanOrEqualTo(before + threads * perThread);
    }

    private static String hashOf(long seq) throws SQLException {
        try (Connection connection = TestDatabase.connect(TestDatabase.OWNER);
             Statement statement = connection.createStatement();
             var rs = statement.executeQuery("SELECT hash FROM audit.audit_event WHERE seq = " + seq)) {
            rs.next();
            return rs.getString(1);
        }
    }
}
