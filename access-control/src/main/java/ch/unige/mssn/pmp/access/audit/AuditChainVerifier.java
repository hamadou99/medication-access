package ch.unige.mssn.pmp.access.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Recalcule toute la chaîne. Une suppression en fin de journal, accompagnée d'une remise à jour de la tête, ne laisse
 * aucun trou : seule la comparaison à un point d'ancrage conservé hors de la base la révèle.
 */
public class AuditChainVerifier {

    private static final String GENESIS = "0".repeat(64);

    private final JdbcTemplate jdbc;
    private final AuditHasher hasher;

    public AuditChainVerifier(JdbcTemplate jdbc, AuditHasher hasher) {
        this.jdbc = jdbc;
        this.hasher = hasher;
    }

    public record Anchor(long seq, String hash) {}

    public record Result(boolean valid, long checked, long lastSeq, String lastHash, Long brokenAt, String problem) {}

    public Result verify() {
        return verify(null);
    }

    public Result verify(Anchor anchor) {
        Walker walker = new Walker(anchor);
        jdbc.query("""
                SELECT seq, event_uuid, occurred_at, person_id, person_name, acting_role, patient_id, resource, action, atc_event,
                       outcome, reason_code, reason_text, purpose_of_use, emergency_justification, reference, policy_version,
                       prev_hash, hash
                FROM audit.audit_event ORDER BY seq
                """, walker);
        return walker.finish(jdbc.queryForObject("SELECT last_seq FROM audit.chain_head WHERE id = 1", Long.class),
                jdbc.queryForObject("SELECT last_hash FROM audit.chain_head WHERE id = 1", String.class));
    }

    private final class Walker implements RowCallbackHandler {
        private final Anchor anchor;
        private long expectedSeq = 1;
        private String previous = GENESIS;
        private long checked;
        private boolean anchorSeen;
        private Long brokenAt;
        private String problem;

        Walker(Anchor anchor) {
            this.anchor = anchor;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            if (brokenAt != null) {
                return;
            }
            long seq = rs.getLong("seq");
            if (seq != expectedSeq) {
                fail(expectedSeq, "Événement manquant");
                return;
            }
            if (!previous.equals(rs.getString("prev_hash"))) {
                fail(seq, "Lien avec l'événement précédent rompu");
                return;
            }
            String computed = hasher.hash(seq, previous, read(rs));
            if (!computed.equals(rs.getString("hash"))) {
                fail(seq, "Contenu de l'événement modifié");
                return;
            }
            if (anchor != null && anchor.seq() == seq) {
                anchorSeen = true;
                if (!anchor.hash().equals(computed)) {
                    fail(seq, "Empreinte différente du point d'ancrage");
                    return;
                }
            }
            previous = computed;
            expectedSeq = seq + 1;
            checked++;
        }

        Result finish(long headSeq, String headHash) {
            if (brokenAt == null && anchor != null && anchor.seq() > 0 && !anchorSeen) {
                fail(anchor.seq(), "Point d'ancrage absent : la fin du journal a disparu");
            }
            if (brokenAt == null && (headSeq != expectedSeq - 1 || !headHash.equals(previous))) {
                fail(expectedSeq, "Tête de chaîne incohérente avec le journal");
            }
            return new Result(brokenAt == null, checked, expectedSeq - 1, previous, brokenAt, problem);
        }

        private void fail(long seq, String why) {
            brokenAt = seq;
            problem = why;
        }

        private AuditEvent read(ResultSet rs) throws SQLException {
            return new AuditEvent(rs.getObject("event_uuid", UUID.class),
                    rs.getTimestamp("occurred_at").toInstant().truncatedTo(ChronoUnit.MICROS),
                    rs.getString("person_id"), rs.getString("person_name"), rs.getString("acting_role"), rs.getString("patient_id"),
                    rs.getString("resource"), rs.getString("action"), rs.getString("atc_event"), rs.getString("outcome"),
                    rs.getString("reason_code"), rs.getString("reason_text"), rs.getString("purpose_of_use"),
                    rs.getString("emergency_justification"), rs.getString("reference"), rs.getString("policy_version"));
        }
    }
}
