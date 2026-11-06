package ch.unige.mssn.pmp.access.audit;

import java.time.Instant;
import java.util.UUID;

/** Trace d'une décision, acceptée ou refusée. */
public record AuditEvent(UUID eventUuid, Instant occurredAt, String personId, String personName, String actingRole,
                         String patientId, String resource, String action, String atcEvent, String outcome,
                         String reasonCode, String reasonText, String purposeOfUse, String emergencyJustification,
                         String reference, String policyVersion) {
}
