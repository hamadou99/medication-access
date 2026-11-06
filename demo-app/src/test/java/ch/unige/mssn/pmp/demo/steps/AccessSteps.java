package ch.unige.mssn.pmp.demo.steps;

import static org.assertj.core.api.Assertions.assertThat;

import ch.unige.mssn.pmp.access.audit.AuditChainVerifier;
import ch.unige.mssn.pmp.access.audit.AuditQueries;
import ch.unige.mssn.pmp.demo.support.ApiClient;
import ch.unige.mssn.pmp.demo.support.Personas;
import ch.unige.mssn.pmp.demo.support.TestTokens;
import io.cucumber.java.Before;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;

public class AccessSteps {

    @Autowired
    private Environment environment;
    @Autowired
    private AuditQueries audit;
    @Autowired
    private AuditChainVerifier verifier;
    @Autowired
    private JdbcClient jdbc;

    private ApiClient api;
    private ApiClient.Answer answer;
    private Instant scenarioStart;
    private String requesterName;

    @Before
    public void start() {
        api = new ApiClient(environment.getRequiredProperty("local.server.port", Integer.class));
        scenarioStart = Instant.now().minusSeconds(2);
    }

    private String token(String person) {
        requesterName = person;
        return TestTokens.forPersona(Personas.named(person));
    }

    private static String plan(String patient) {
        return "/api/patients/" + Personas.patientId(patient) + "/plan";
    }

    private String treatment(String patient, String medication) {
        return jdbc.sql("SELECT id::text FROM plan.treatment WHERE patient_id = ? AND medication = ? ORDER BY created_at DESC LIMIT 1")
                .params(Personas.patientId(patient), medication)
                .query(String.class)
                .single();
    }

    private String treatmentPath(String patient, String medication) {
        return plan(patient) + "/traitements/" + treatment(patient, medication);
    }

    private static String newTreatment(String medication, String level) {
        return ApiClient.json(Map.of("atc", "A01AA01", "medication", medication, "dosage", "Un comprimé par jour", "level", level));
    }

    @Soit("{string} a inscrit le traitement {string} au plan de {string}")
    public void registered(String person, String medication, String patient) {
        answer = api.post(token(person), plan(patient) + "/traitements", newTreatment(medication, "NORMAL"));
        assertThat(answer.status()).isEqualTo(201);
    }

    @Soit("{string} a retiré le droit d'accès de {string} au dossier de {string}")
    public void revokedGrant(String who, String person, String patient) {
        // Écriture faite par le propriétaire de la base : l'application, elle, n'a que la lecture sur les droits.
        try (var connection = ch.unige.mssn.pmp.demo.support.TestDatabase.connect(ch.unige.mssn.pmp.demo.support.TestDatabase.OWNER);
             var statement = connection.prepareStatement("DELETE FROM access.access_grant WHERE person_id = ? AND patient_id = ?")) {
            statement.setString(1, Personas.named(person).personId());
            statement.setString(2, Personas.patientId(patient));
            assertThat(statement.executeUpdate()).isEqualTo(1);
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Quand("{string} consulte le plan de {string}")
    public void consult(String person, String patient) {
        answer = api.get(token(person), plan(patient));
    }

    @Quand("{string} inscrit le traitement {string} au plan de {string}")
    public void register(String person, String medication, String patient) {
        answer = api.post(token(person), plan(patient) + "/traitements", newTreatment(medication, "NORMAL"));
    }

    @Quand("{string} inscrit le traitement {string} de niveau {string} au plan de {string}")
    public void registerWithLevel(String person, String medication, String level, String patient) {
        answer = api.post(token(person), plan(patient) + "/traitements", newTreatment(medication, level));
    }

    @Quand("{string} prescrit le traitement {string} de {string}")
    public void prescribe(String person, String medication, String patient) {
        answer = api.post(token(person), treatmentPath(patient, medication) + "/prescriptions", null);
    }

    @Quand("{string} enregistre une délivrance du traitement {string} de {string}")
    public void dispense(String person, String medication, String patient) {
        answer = api.post(token(person), treatmentPath(patient, medication) + "/delivrances", null);
    }

    @Quand("{string} émet un avis pharmaceutique sur le traitement {string} de {string}")
    public void advise(String person, String medication, String patient) {
        answer = api.post(token(person), treatmentPath(patient, medication) + "/avis", ApiClient.json(Map.of("note", "Interaction possible, à discuter")));
    }

    @Quand("{string} applique le code {string} au traitement {string} de {string}")
    public void modify(String person, String code, String medication, String patient) {
        answer = api.patch(token(person), treatmentPath(patient, medication), ApiClient.json(Map.of("code", code, "note", "Décision médicale", "dosage", "Deux comprimés par jour")));
    }

    @Quand("{string} demande l'accès d'urgence au plan de {string} avec la justification {string}")
    public void emergency(String person, String patient, String justification) {
        answer = api.post(token(person), plan(patient) + "/acces-urgence", ApiClient.json(Map.of("justification", justification)));
    }

    @Quand("{string} consulte le journal de {string}")
    public void journal(String person, String patient) {
        answer = api.get(token(person), "/api/patients/" + Personas.patientId(patient) + "/journal?from="
                + URLEncoder.encode(scenarioStart.toString(), StandardCharsets.UTF_8));
    }

    @Quand("{string} consulte le journal de {string} au format CH:ATC")
    public void journalFhir(String person, String patient) {
        answer = api.get(token(person), "/api/patients/" + Personas.patientId(patient) + "/journal/fhir?from="
                + URLEncoder.encode(scenarioStart.toString(), StandardCharsets.UTF_8));
    }

    @Quand("{string} liste les accès d'urgence à contrôler")
    public void listEmergencies(String person) {
        answer = api.get(token(person), "/api/controle/acces-urgence");
    }

    @Quand("{string} rend le verdict {string} sur le dernier accès d'urgence de {string}")
    public void review(String person, String verdict, String accessor) {
        answer = api.post(token(person), "/api/controle/acces-urgence/" + lastEvent(accessor).eventUuid() + "/revue",
                ApiClient.json(Map.of("verdict", verdict, "comment", "Contrôlé")));
    }

    @Quand("{string} consulte les anomalies avec un seuil de {int} refus")
    public void anomalies(String person, int threshold) {
        answer = api.get(token(person), "/api/controle/anomalies?deniedThreshold=" + threshold);
    }

    @Quand("{string} vérifie l'intégrité du journal")
    public void integrity(String person) {
        answer = api.get(token(person), "/api/controle/integrite");
    }

    @Alors("la demande est acceptée")
    public void accepted() {
        assertThat(answer.accepted()).as("réponse %s : %s", answer.status(), answer.body()).isTrue();
    }

    @Alors("la demande est refusée")
    public void refused() {
        assertThat(answer.status()).as(answer.body()).isEqualTo(403);
    }

    @Alors("la demande est acceptée au titre de l'urgence")
    public void acceptedAsEmergency() {
        accepted();
        assertThat(answer.json().get("accesUrgence").asBoolean()).isTrue();
    }

    @Alors("la réponse a le statut {int}")
    public void status(int status) {
        assertThat(answer.status()).as(answer.body()).isEqualTo(status);
    }

    @Et("le plan affiché contient {string}")
    public void planContains(String medication) {
        assertThat(medications()).contains(medication);
    }

    @Et("le plan affiché ne contient pas {string}")
    public void planLacks(String medication) {
        assertThat(medications()).doesNotContain(medication);
    }

    @Et("le niveau accordé est {string}")
    public void level(String level) {
        assertThat(answer.json().get("niveauAccorde").asString()).isEqualTo(level);
    }

    @Et("le message explique {string}")
    public void message(String expected) {
        assertThat(answer.json().get("detail").asString()).contains(expected);
    }

    @Et("le message ne mentionne pas {string}")
    public void messageLacks(String word) {
        assertThat(answer.body().toLowerCase()).doesNotContain(word.toLowerCase());
    }

    @Et("le journal garde une trace {string} au motif {string}")
    public void trace(String outcome, String reason) {
        AuditQueries.Entry entry = lastEvent(requesterName);
        assertThat(entry.outcome()).isEqualTo(outcome);
        assertThat(entry.reason()).isEqualTo(reason);
    }

    @Et("cette trace désigne {string}, {string} et l'action {string}")
    public void traceContent(String person, String role, String action) {
        AuditQueries.Entry entry = lastEvent(requesterName);
        assertThat(entry.personName()).isEqualTo(Personas.named(person).displayName());
        assertThat(entry.actingRole()).isEqualTo(role);
        assertThat(entry.action()).isEqualTo(action);
        assertThat(entry.occurredAt()).isAfter(scenarioStart).isBefore(Instant.now().plusSeconds(1));
    }

    @Et("cette trace indique l'urgence avec la justification {string}")
    public void traceEmergency(String justification) {
        AuditQueries.Entry entry = lastEvent(requesterName);
        assertThat(entry.purposeOfUse()).isEqualTo("EMER");
        assertThat(entry.emergencyJustification()).isEqualTo(justification);
    }

    @Et("{string} a une information en attente")
    public void notification(String patient) {
        assertThat(audit.pendingNotifications(Personas.patientId(patient))).isGreaterThan(0);
    }

    @Et("{string} n'a aucune information nouvelle en attente")
    public void noNotification(String patient) {
        Long recent = jdbc.sql("SELECT count(*) FROM audit.patient_notification WHERE patient_id = ? AND created_at >= ?")
                .params(Personas.patientId(patient), java.sql.Timestamp.from(scenarioStart))
                .query(Long.class).single();
        assertThat(recent).isZero();
    }

    @Et("le journal affiché contient une trace {string} de {string}")
    public void journalShows(String outcome, String person) {
        List<JsonNode> entries = answer.json().valueStream().toList();
        assertThat(entries).anySatisfy(entry -> {
            assertThat(entry.get("outcome").asString()).isEqualTo(outcome);
            assertThat(entry.get("personName").asString()).isEqualTo(Personas.named(person).displayName());
        });
    }

    @Et("le journal affiché contient l'action {string}")
    public void journalShowsAction(String action) {
        assertThat(answer.json().valueStream().map(e -> e.get("action").asString()).toList()).contains(action);
    }

    @Et("la réponse FHIR contient un AuditEvent {string} avec le but {string}")
    public void fhirEvent(String atcEvent, String purpose) {
        JsonNode entries = answer.json().get("entry");
        assertThat(entries.valueStream().map(e -> e.get("resource")).toList()).anySatisfy(event -> {
            assertThat(event.get("resourceType").asString()).isEqualTo("AuditEvent");
            assertThat(event.get("subtype").get(0).get("code").asString()).isEqualTo(atcEvent);
            assertThat(event.get("purposeOfEvent").get(0).get("coding").get(0).get("code").asString()).isEqualTo(purpose);
        });
    }

    @Et("la liste de contrôle contient l'accès de {string}")
    public void reviewListContains(String person) {
        assertThat(answer.json().valueStream().map(e -> e.get("personId").asString()).toList()).contains(Personas.named(person).personId());
    }

    @Et("la liste de contrôle ne contient plus ce dernier accès de {string}")
    public void reviewListLacks(String person) {
        var last = lastEvent(person).eventUuid().toString();
        assertThat(answer.json().valueStream().map(e -> e.get("eventUuid").asString()).toList()).doesNotContain(last);
    }

    @Et("les anomalies signalent {string} pour {string}")
    public void anomaly(String kind, String person) {
        assertThat(answer.json().valueStream().toList()).anySatisfy(entry -> {
            assertThat(entry.get("kind").asString()).isEqualTo(kind);
            assertThat(entry.get("personId").asString()).isEqualTo(Personas.named(person).personId());
        });
    }

    @Et("le journal est déclaré intègre")
    public void journalIntact() {
        assertThat(answer.json().get("valid").asBoolean()).isTrue();
        assertThat(verifier.verify().valid()).isTrue();
    }

    @Et("{int} document CH EMED de type {string} existe pour le traitement {string} de {string}")
    public void documents(int count, String type, String medication, String patient) {
        Long total = jdbc.sql("SELECT count(*) FROM plan.document WHERE treatment_id = ?::uuid AND type = ?")
                .params(treatment(patient, medication), type)
                .query(Long.class).single();
        assertThat(total).isEqualTo(count);
    }

    @Et("le traitement {string} de {string} a le statut {string}")
    public void treatmentStatus(String medication, String patient, String status) {
        String actual = jdbc.sql("SELECT status FROM plan.treatment WHERE id = ?::uuid")
                .param(treatment(patient, medication)).query(String.class).single();
        assertThat(actual).isEqualTo(status);
    }

    @Quand("{string} demande l'accès d'urgence au plan de {string} avec une justification sur deux lignes {string} et {string}")
    public void emergencyOnTwoLines(String person, String patient, String first, String second) {
        emergency(person, patient, first + "\n" + second);
    }

    private List<String> medications() {
        return answer.json().get("traitements").valueStream().map(t -> t.get("medication").asString()).toList();
    }

    private AuditQueries.Entry lastEvent(String person) {
        return jdbc.sql("SELECT event_uuid FROM audit.audit_event WHERE person_id = ? ORDER BY seq DESC LIMIT 1")
                .param(Personas.named(person).personId())
                .query(java.util.UUID.class)
                .optional()
                .flatMap(audit::byUuid)
                .orElseThrow(() -> new AssertionError("Aucune trace pour " + person));
    }
}
