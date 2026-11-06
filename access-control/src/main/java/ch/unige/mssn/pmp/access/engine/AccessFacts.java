package ch.unige.mssn.pmp.access.engine;

import ch.unige.mssn.pmp.access.model.Confidentiality;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/** Faits sur lesquels repose la décision. Ils ne sont pas mis en cache : un retrait s'applique à la demande suivante. */
public interface AccessFacts {

    Optional<PatientSettings> patientSettings(String patientId);

    boolean isExcluded(String personId, String patientId);

    Optional<Grant> grant(String personId, String patientId);

    Optional<Mandate> mandate(String personId, String patientId);

    enum EmergencyMode { DISABLED, NORMAL_ONLY, NORMAL_AND_RESTRICTED }

    record PatientSettings(boolean consentActive, EmergencyMode emergencyMode) {}

    record Grant(Confidentiality level, Instant validFrom, Instant validUntil) {
        public boolean validAt(Instant at) {
            return !at.isBefore(validFrom) && (validUntil == null || at.isBefore(validUntil));
        }
    }

    record Mandate(Instant validFrom, Instant validUntil, boolean revoked, Set<String> actions) {
        public boolean validAt(Instant at) {
            return !revoked && !at.isBefore(validFrom) && (validUntil == null || at.isBefore(validUntil));
        }

        public boolean covers(String action) {
            return actions.contains(action);
        }
    }
}
