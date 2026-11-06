package ch.unige.mssn.pmp.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.unige.mssn.pmp.access.audit.AuditEvent;
import ch.unige.mssn.pmp.access.audit.AuditHasher;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditHasherTest {

    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Instant AT = Instant.parse("2027-03-12T09:42:00Z");
    private static final String GENESIS = "0".repeat(64);

    private final AuditHasher hasher = new AuditHasher(KEY);

    private static AuditEvent event(String person, String reason) {
        return new AuditEvent(ID, AT, person, "Dre Martin", "MEDECIN", "P1", "PLAN_MEDICATION", "PRESCRIRE", "ATC_DOC_CREATE",
                "PERMIT", "CARE_RELATIONSHIP", reason, "NORM", null, null, "v1");
    }

    @Test
    void sameContentGivesSameHash() {
        assertThat(hasher.hash(1, GENESIS, event("a", "b"))).isEqualTo(hasher.hash(1, GENESIS, event("a", "b"))).hasSize(64);
    }

    @Test
    void contentPositionAndPreviousHashAreAllCovered() {
        String reference = hasher.hash(1, GENESIS, event("a", "b"));
        assertThat(hasher.hash(1, GENESIS, event("x", "b"))).isNotEqualTo(reference);
        assertThat(hasher.hash(2, GENESIS, event("a", "b"))).isNotEqualTo(reference);
        assertThat(hasher.hash(1, "1".repeat(64), event("a", "b"))).isNotEqualTo(reference);
    }

    @Test
    void movingTextBetweenFieldsChangesTheHash() {
        assertThat(hasher.hash(1, GENESIS, event("ab", "c"))).isNotEqualTo(hasher.hash(1, GENESIS, event("a", "bc")));
    }

    @Test
    void nullAndEmptyAreDistinct() {
        AuditEvent withNull = event("a", "b");
        AuditEvent withEmpty = new AuditEvent(ID, AT, "a", "Dre Martin", "MEDECIN", "P1", "PLAN_MEDICATION", "PRESCRIRE", "ATC_DOC_CREATE",
                "PERMIT", "CARE_RELATIONSHIP", "b", "NORM", "", null, "v1");
        assertThat(hasher.hash(1, GENESIS, withNull)).isNotEqualTo(hasher.hash(1, GENESIS, withEmpty));
    }

    @Test
    void anotherKeyGivesAnotherHash() {
        AuditHasher other = new AuditHasher("fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8));
        assertThat(other.hash(1, GENESIS, event("a", "b"))).isNotEqualTo(hasher.hash(1, GENESIS, event("a", "b")));
    }

    @Test
    void shortKeyIsRefused() {
        assertThatThrownBy(() -> new AuditHasher(new byte[31])).isInstanceOf(IllegalArgumentException.class);
        new AuditHasher(new byte[32]);
    }
}
