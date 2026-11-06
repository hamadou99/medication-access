package ch.unige.mssn.pmp.access.audit;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Écrit le journal dans sa propre transaction : la trace d'un refus survit à l'annulation de la demande, et une
 * trace impossible à écrire empêche l'action d'avoir lieu.
 */
public class AuditLog {

    private static final String EMERGENCY_OUTCOME = "EMERGENCY_PERMIT";

    private final JdbcClient jdbc;
    private final TransactionTemplate newTransaction;
    private final AuditHasher hasher;

    public AuditLog(JdbcClient jdbc, PlatformTransactionManager transactions, AuditHasher hasher) {
        this.jdbc = jdbc;
        this.hasher = hasher;
        this.newTransaction = new TransactionTemplate(transactions);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** @return le numéro de séquence attribué */
    public long append(AuditEvent event) {
        Long seq = newTransaction.execute(status -> {
            Head head = jdbc.sql("SELECT last_seq, last_hash FROM audit.chain_head WHERE id = 1 FOR UPDATE")
                    .query((rs, n) -> new Head(rs.getLong(1), rs.getString(2)))
                    .single();
            long next = head.seq() + 1;
            String hash = hasher.hash(next, head.hash(), event);
            insert(next, head.hash(), hash, event);
            jdbc.sql("UPDATE audit.chain_head SET last_seq = ?, last_hash = ? WHERE id = 1")
                    .params(next, hash)
                    .update();
            if (EMERGENCY_OUTCOME.equals(event.outcome())) {
                jdbc.sql("INSERT INTO audit.patient_notification (event_seq, patient_id, created_at) VALUES (?, ?, ?)")
                        .params(next, event.patientId(), timestamp(event.occurredAt()))
                        .update();
            }
            return next;
        });
        return seq;
    }

    private void insert(long seq, String previousHash, String hash, AuditEvent e) {
        jdbc.sql("""
                INSERT INTO audit.audit_event (seq, event_uuid, occurred_at, person_id, person_name, acting_role, patient_id,
                    resource, action, atc_event, outcome, reason_code, reason_text, purpose_of_use, emergency_justification,
                    reference, policy_version, prev_hash, hash)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)
                .params(seq, e.eventUuid(), timestamp(e.occurredAt()), e.personId(), e.personName(), e.actingRole(), e.patientId(),
                        e.resource(), e.action(), e.atcEvent(), e.outcome(), e.reasonCode(), e.reasonText(), e.purposeOfUse(),
                        e.emergencyJustification(), e.reference(), e.policyVersion(), previousHash, hash)
                .update();
    }

    private static Timestamp timestamp(Instant at) {
        return Timestamp.from(at);
    }

    private record Head(long seq, String hash) {}
}
