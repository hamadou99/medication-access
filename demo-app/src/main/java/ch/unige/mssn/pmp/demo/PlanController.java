package ch.unige.mssn.pmp.demo;

import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Subject;
import ch.unige.mssn.pmp.access.spring.AccessContext;
import ch.unige.mssn.pmp.access.spring.AccessControlled;
import ch.unige.mssn.pmp.access.spring.SubjectResolver;
import ch.unige.mssn.pmp.demo.ChEmedDocuments.Type;
import ch.unige.mssn.pmp.demo.TreatmentRepository.Treatment;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Fonctions du plan de médication. Chaque méthode déclare son action ; le module décide avant qu'elle ne s'exécute. */
@RestController
@RequestMapping("/api/patients/{patientId}/plan")
public class PlanController {

    private static final String RESOURCE = "PLAN_MEDICATION";
    private static final String TREATMENT_LEVEL = "@treatments.level(#patientId, #treatmentId)";
    private static final String TREATMENT_REF = "#treatmentId.toString()";

    private final TreatmentRepository treatments;
    private final ChEmedDocuments documents;
    private final SubjectResolver subjects;
    private final TransactionTemplate transaction;
    private final Clock clock;

    public PlanController(TreatmentRepository treatments, ChEmedDocuments documents, SubjectResolver subjects,
                          TransactionTemplate transaction, Clock clock) {
        this.treatments = treatments;
        this.documents = documents;
        this.subjects = subjects;
        this.transaction = transaction;
        this.clock = clock;
    }

    public record NewTreatment(String atc, String medication, String dosage, Confidentiality level) {
        public NewTreatment {
            level = level == null ? Confidentiality.NORMAL : level;
        }
    }

    public record EmergencyRequest(String justification) {
        public EmergencyRequest {
            justification = justification == null ? "" : justification;
        }
    }

    public record Advice(String code, String note, String dosage) {}

    public record TreatmentView(UUID id, String atc, String medication, String dosage, Confidentiality level, String status) {
        static TreatmentView of(Treatment t) {
            return new TreatmentView(t.id(), t.atc(), t.medication(), t.dosage(), t.level(), t.status());
        }
    }

    /** {@code niveauAccorde} dit jusqu'où la liste va, sans révéler si des traitements plus protégés existent. */
    public record PlanView(String patientId, Confidentiality niveauAccorde, boolean accesUrgence, List<TreatmentView> traitements) {}

    @GetMapping
    @AccessControlled(resource = RESOURCE, action = "CONSULTER_PLAN", patient = "#patientId")
    public PlanView consult(@PathVariable String patientId) {
        return currentPlan(patientId);
    }

    @PostMapping("/acces-urgence")
    @AccessControlled(resource = RESOURCE, action = "CONSULTER_PLAN", patient = "#patientId", emergencyJustification = "#request.justification()")
    public PlanView consultInEmergency(@PathVariable String patientId, @RequestBody EmergencyRequest request) {
        return currentPlan(patientId);
    }

    private PlanView currentPlan(String patientId) {
        var decision = AccessContext.current();
        List<TreatmentView> visible = treatments.visibleFor(patientId, decision.clearance()).stream().map(TreatmentView::of).toList();
        return new PlanView(patientId, decision.clearance(), decision.outcome().name().equals("EMERGENCY_PERMIT"), visible);
    }

    @PostMapping("/traitements")
    @ResponseStatus(HttpStatus.CREATED)
    @AccessControlled(resource = RESOURCE, action = "INSCRIRE_TRAITEMENT", patient = "#patientId", dataLevel = "#body.level()")
    public TreatmentView addTreatment(@PathVariable String patientId, @RequestBody NewTreatment body) {
        requireText(body.medication(), "medication");
        requireText(body.atc(), "atc");
        requireText(body.dosage(), "dosage");
        Instant now = clock.instant();
        Treatment treatment = new Treatment(UUID.randomUUID(), UUID.randomUUID(), patientId, body.atc(), body.medication(), body.dosage(), body.level(), "active");
        String fhir = render(Type.MTP, treatment.documentId(), treatment, null, null, now);
        transaction.executeWithoutResult(status -> {
            treatments.insert(treatment, author().gln(), now);
            treatments.insertDocument(treatment.documentId(), treatment.id(), patientId, Type.MTP, treatment.level(), author().gln(), now, fhir);
        });
        return TreatmentView.of(treatment);
    }

    @PostMapping("/traitements/{treatmentId}/prescriptions")
    @ResponseStatus(HttpStatus.CREATED)
    @AccessControlled(resource = RESOURCE, action = "PRESCRIRE", patient = "#patientId", dataLevel = TREATMENT_LEVEL, reference = TREATMENT_REF)
    public Map<String, UUID> prescribe(@PathVariable String patientId, @PathVariable UUID treatmentId) {
        return addDocument(requireActive(patientId, treatmentId), Type.PRE, null, null);
    }

    @PostMapping("/traitements/{treatmentId}/delivrances")
    @ResponseStatus(HttpStatus.CREATED)
    @AccessControlled(resource = RESOURCE, action = "ENREGISTRER_DELIVRANCE", patient = "#patientId", dataLevel = TREATMENT_LEVEL, reference = TREATMENT_REF)
    public Map<String, UUID> dispense(@PathVariable String patientId, @PathVariable UUID treatmentId) {
        return addDocument(requireActive(patientId, treatmentId), Type.DIS, null, null);
    }

    @PostMapping("/traitements/{treatmentId}/avis")
    @ResponseStatus(HttpStatus.CREATED)
    @AccessControlled(resource = RESOURCE, action = "EMETTRE_AVIS_PHARMACEUTIQUE", patient = "#patientId", dataLevel = TREATMENT_LEVEL, reference = TREATMENT_REF)
    public Map<String, UUID> advise(@PathVariable String patientId, @PathVariable UUID treatmentId, @RequestBody Advice advice) {
        requireText(advice.note(), "note");
        return addDocument(require(patientId, treatmentId), Type.PADV, "COMMENT", advice.note());
    }

    /** Applique le cycle de vie CH EMED : suspendu peut redevenir actif ; annulé et refusé sont définitifs. */
    @PatchMapping("/traitements/{treatmentId}")
    @AccessControlled(resource = RESOURCE, action = "MODIFIER_TRAITEMENT", patient = "#patientId", dataLevel = TREATMENT_LEVEL, reference = TREATMENT_REF)
    public TreatmentView modify(@PathVariable String patientId, @PathVariable UUID treatmentId, @RequestBody Advice advice) {
        Treatment current = require(patientId, treatmentId);
        String code = advice.code() == null ? "" : advice.code();
        String next = switch (code) {
            case "SUSPEND" -> transition(current, "active", "suspended");
            case "OK" -> transition(current, "suspended", "active");
            case "CANCEL" -> transition(current, "active|suspended", "cancelled");
            case "REFUSE" -> transition(current, "active|suspended", "refused");
            case "CHANGE" -> transition(current, "active", "active");
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code d'avis inconnu");
        };
        String dosage = code.equals("CHANGE") && advice.dosage() != null ? advice.dosage() : current.dosage();
        Instant now = clock.instant();
        UUID documentId = UUID.randomUUID();
        String fhir = render(Type.PADV, documentId, current, code, advice.note() == null ? code : advice.note(), now);
        transaction.executeWithoutResult(status -> {
            treatments.updateStatus(current.id(), next, dosage);
            treatments.insertDocument(documentId, current.id(), patientId, Type.PADV, current.level(), author().gln(), now, fhir);
        });
        return TreatmentView.of(new Treatment(current.id(), current.documentId(), patientId, current.atc(), current.medication(), dosage, current.level(), next));
    }

    private Map<String, UUID> addDocument(Treatment treatment, Type type, String adviceCode, String note) {
        Instant now = clock.instant();
        UUID documentId = UUID.randomUUID();
        String fhir = render(type, documentId, treatment, adviceCode, note, now);
        treatments.insertDocument(documentId, treatment.id(), treatment.patientId(), type, treatment.level(), author().gln(), now, fhir);
        return Map.of("document", documentId);
    }

    private String render(Type type, UUID documentId, Treatment t, String adviceCode, String note, Instant at) {
        var patient = treatments.patient(t.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dossier introuvable"));
        return documents.render(new ChEmedDocuments.Content(type, documentId.toString(), UUID.randomUUID().toString(), t.id().toString(), t.documentId().toString(),
                patient, author(), t.level(), t.atc(), t.medication(), t.dosage(), adviceCode, note, at));
    }

    private ChEmedDocuments.Author author() {
        Subject subject = subjects.current();
        return ChEmedDocuments.Author.of(subject.personId(), subject.displayName());
    }

    private Treatment require(String patientId, UUID treatmentId) {
        return treatments.find(patientId, treatmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Traitement introuvable"));
    }

    private Treatment requireActive(String patientId, UUID treatmentId) {
        Treatment treatment = require(patientId, treatmentId);
        if (!treatment.status().equals("active")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce traitement n'est pas actif");
        }
        return treatment;
    }

    private static String transition(Treatment current, String allowedFrom, String to) {
        if (!current.status().matches(allowedFrom)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Changement impossible depuis l'état " + current.status());
        }
        return to;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Champ obligatoire : " + field);
        }
    }
}
