package ch.unige.mssn.pmp.access.audit;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Lectures du journal : information du patient, contrôle des accès d'urgence et des accès inhabituels. */
public class AuditQueries {

    private static final String ENTRY_COLUMNS = """
            seq, event_uuid, occurred_at, person_id, person_name, acting_role, patient_id, resource, action, atc_event,
            outcome, reason_text, purpose_of_use, emergency_justification, reference
            """;

    private final JdbcClient jdbc;

    public AuditQueries(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record Entry(long seq, UUID eventUuid, Instant occurredAt, String personId, String personName, String actingRole,
                        String patientId, String resource, String action, String atcEvent, String outcome, String reason,
                        String purposeOfUse, String emergencyJustification, String reference) {}

    public record Anomaly(String kind, String personId, long count) {}

    public enum Verdict { JUSTIFIE, A_INSTRUIRE, ABUSIF }

    public List<Entry> journal(String patientId, Instant from, Instant to, int limit) {
        return jdbc.sql("SELECT " + ENTRY_COLUMNS + """
                FROM audit.audit_event
                WHERE patient_id = ? AND occurred_at >= ? AND occurred_at < ?
                ORDER BY seq DESC LIMIT ?
                """)
                .params(patientId, Timestamp.from(from), Timestamp.from(to), limit)
                .query(AuditQueries::entry)
                .list();
    }

    public List<Entry> emergencyAccessesToReview(int limit) {
        return jdbc.sql("SELECT " + ENTRY_COLUMNS + """
                FROM audit.audit_event e
                WHERE outcome = 'EMERGENCY_PERMIT'
                  AND NOT EXISTS (SELECT 1 FROM audit.emergency_review r WHERE r.event_seq = e.seq)
                ORDER BY seq LIMIT ?
                """)
                .param(limit)
                .query(AuditQueries::entry)
                .list();
    }

    public Optional<Entry> byUuid(UUID eventUuid) {
        return jdbc.sql("SELECT " + ENTRY_COLUMNS + "FROM audit.audit_event WHERE event_uuid = ?")
                .param(eventUuid)
                .query(AuditQueries::entry)
                .optional();
    }

    public void recordReview(long eventSeq, String reviewerId, Verdict verdict, String comment, Instant at) {
        jdbc.sql("INSERT INTO audit.emergency_review (event_seq, reviewer_id, verdict, comment, reviewed_at) VALUES (?, ?, ?, ?, ?)")
                .params(eventSeq, reviewerId, verdict.name(), comment, Timestamp.from(at))
                .update();
    }

    /** Personnes dont le nombre de refus ou d'accès d'urgence depuis {@code since} atteint le seuil. */
    public List<Anomaly> anomalies(Instant since, int deniedThreshold, int emergencyThreshold) {
        return jdbc.sql("""
                SELECT 'REFUS_REPETES' AS kind, person_id, count(*) AS total FROM audit.audit_event
                WHERE outcome = 'DENY' AND occurred_at >= ? GROUP BY person_id HAVING count(*) >= ?
                UNION ALL
                SELECT 'URGENCES_REPETEES', person_id, count(*) FROM audit.audit_event
                WHERE outcome = 'EMERGENCY_PERMIT' AND occurred_at >= ? GROUP BY person_id HAVING count(*) >= ?
                ORDER BY total DESC, person_id
                """)
                .params(Timestamp.from(since), deniedThreshold, Timestamp.from(since), emergencyThreshold)
                .query((rs, n) -> new Anomaly(rs.getString(1), rs.getString(2), rs.getLong(3)))
                .list();
    }

    public long pendingNotifications(String patientId) {
        return jdbc.sql("SELECT count(*) FROM audit.patient_notification WHERE patient_id = ? AND sent_at IS NULL")
                .param(patientId)
                .query(Long.class)
                .single();
    }

    private static Entry entry(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return new Entry(rs.getLong("seq"), rs.getObject("event_uuid", UUID.class), rs.getTimestamp("occurred_at").toInstant(),
                rs.getString("person_id"), rs.getString("person_name"), rs.getString("acting_role"), rs.getString("patient_id"),
                rs.getString("resource"), rs.getString("action"), rs.getString("atc_event"), rs.getString("outcome"),
                rs.getString("reason_text"), rs.getString("purpose_of_use"), rs.getString("emergency_justification"),
                rs.getString("reference"));
    }
}
