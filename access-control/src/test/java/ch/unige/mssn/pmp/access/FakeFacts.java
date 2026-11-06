package ch.unige.mssn.pmp.access;

import ch.unige.mssn.pmp.access.engine.AccessFacts;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Faits en mémoire pour vérifier les règles sans base de données. */
final class FakeFacts implements AccessFacts {

    static final Instant NOW = Instant.parse("2027-03-12T09:42:00Z");
    static final Instant PAST = NOW.minusSeconds(86_400);
    static final Instant FUTURE = NOW.plusSeconds(86_400);

    private final Map<String, PatientSettings> settings = new HashMap<>();
    private final Map<String, Grant> grants = new HashMap<>();
    private final Map<String, Mandate> mandates = new HashMap<>();
    private final Set<String> exclusions = new HashSet<>();

    FakeFacts patient(String patientId) {
        return patient(patientId, true, EmergencyMode.NORMAL_ONLY);
    }

    FakeFacts patient(String patientId, boolean consent, EmergencyMode mode) {
        settings.put(patientId, new PatientSettings(consent, mode));
        return this;
    }

    FakeFacts grant(String person, String patientId, Confidentiality level) {
        return grant(person, patientId, level, PAST, null);
    }

    FakeFacts grant(String person, String patientId, Confidentiality level, Instant from, Instant until) {
        grants.put(person + patientId, new Grant(level, from, until));
        return this;
    }

    FakeFacts mandate(String person, String patientId, Instant from, Instant until, boolean revoked, Set<String> actions) {
        mandates.put(person + patientId, new Mandate(from, until, revoked, actions));
        return this;
    }

    FakeFacts exclude(String person, String patientId) {
        exclusions.add(person + patientId);
        return this;
    }

    @Override
    public Optional<PatientSettings> patientSettings(String patientId) {
        return Optional.ofNullable(settings.get(patientId));
    }

    @Override
    public boolean isExcluded(String personId, String patientId) {
        return exclusions.contains(personId + patientId);
    }

    @Override
    public Optional<Grant> grant(String personId, String patientId) {
        return Optional.ofNullable(grants.get(personId + patientId));
    }

    @Override
    public Optional<Mandate> mandate(String personId, String patientId) {
        return Optional.ofNullable(mandates.get(personId + patientId));
    }
}
