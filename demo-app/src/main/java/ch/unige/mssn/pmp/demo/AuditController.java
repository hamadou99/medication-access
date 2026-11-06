package ch.unige.mssn.pmp.demo;

import ch.unige.mssn.pmp.access.audit.AtcAuditEventMapper;
import ch.unige.mssn.pmp.access.audit.AuditChainVerifier;
import ch.unige.mssn.pmp.access.audit.AuditQueries;
import ch.unige.mssn.pmp.access.model.Subject;
import ch.unige.mssn.pmp.access.spring.AccessControlled;
import ch.unige.mssn.pmp.access.spring.SubjectResolver;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Journal : information du patient d'un côté, contrôle par la fonction de surveillance de l'autre. */
@RestController
public class AuditController {

    private static final String RESOURCE = "JOURNAL_ACCES";
    private static final int MAX_ENTRIES = 500;

    private final AuditQueries queries;
    private final AuditChainVerifier verifier;
    private final SubjectResolver subjects;
    private final Clock clock;

    public AuditController(AuditQueries queries, AuditChainVerifier verifier, SubjectResolver subjects, Clock clock) {
        this.queries = queries;
        this.verifier = verifier;
        this.subjects = subjects;
        this.clock = clock;
    }

    public record Review(AuditQueries.Verdict verdict, String comment) {}

    @GetMapping("/api/patients/{patientId}/journal")
    @AccessControlled(resource = RESOURCE, action = "CONSULTER_JOURNAL", patient = "#patientId")
    public List<AuditQueries.Entry> journal(@PathVariable String patientId,
                                            @RequestParam(required = false) Instant from,
                                            @RequestParam(required = false) Instant to,
                                            @RequestParam(defaultValue = "100") int limit) {
        return read(patientId, from, to, limit);
    }

    /** Même contenu au format AuditEvent de CH:ATC : c'est une seconde lecture, donc une seconde trace. */
    @GetMapping(value = "/api/patients/{patientId}/journal/fhir", produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessControlled(resource = RESOURCE, action = "CONSULTER_JOURNAL", patient = "#patientId")
    public Map<String, Object> journalAsFhir(@PathVariable String patientId,
                                             @RequestParam(required = false) Instant from,
                                             @RequestParam(required = false) Instant to,
                                             @RequestParam(defaultValue = "100") int limit) {
        return AtcAuditEventMapper.toBundle(read(patientId, from, to, limit));
    }

    private List<AuditQueries.Entry> read(String patientId, Instant from, Instant to, int limit) {
        Instant end = to == null ? clock.instant().plus(1, ChronoUnit.SECONDS) : to;
        Instant start = from == null ? end.minus(365, ChronoUnit.DAYS) : from;
        return queries.journal(patientId, start, end, Math.clamp(limit, 1, MAX_ENTRIES));
    }

    @GetMapping("/api/controle/acces-urgence")
    @AccessControlled(resource = RESOURCE, action = "CONTROLER_ACCES")
    public List<AuditQueries.Entry> emergencyAccessesToReview() {
        return queries.emergencyAccessesToReview(MAX_ENTRIES);
    }

    @PostMapping("/api/controle/acces-urgence/{eventUuid}/revue")
    @AccessControlled(resource = RESOURCE, action = "CONTROLER_ACCES", reference = "#eventUuid.toString()")
    public Map<String, String> review(@PathVariable UUID eventUuid, @RequestBody Review review) {
        if (review.verdict() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verdict obligatoire");
        }
        AuditQueries.Entry event = queries.byUuid(eventUuid)
                .filter(entry -> entry.outcome().equals("EMERGENCY_PERMIT"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accès d'urgence introuvable"));
        Subject reviewer = subjects.current();
        if (event.personId().equals(reviewer.personId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Personne ne contrôle son propre accès d'urgence");
        }
        queries.recordReview(event.seq(), reviewer.personId(), review.verdict(), review.comment(), clock.instant());
        return Map.of("verdict", review.verdict().name());
    }

    @GetMapping("/api/controle/anomalies")
    @AccessControlled(resource = RESOURCE, action = "CONTROLER_ACCES")
    public List<AuditQueries.Anomaly> anomalies(@RequestParam(defaultValue = "24") int hours,
                                                @RequestParam(defaultValue = "5") int deniedThreshold,
                                                @RequestParam(defaultValue = "3") int emergencyThreshold) {
        return queries.anomalies(clock.instant().minus(hours, ChronoUnit.HOURS), deniedThreshold, emergencyThreshold);
    }

    @GetMapping("/api/controle/integrite")
    @AccessControlled(resource = RESOURCE, action = "CONTROLER_ACCES")
    public AuditChainVerifier.Result integrity() {
        return verifier.verify();
    }
}
