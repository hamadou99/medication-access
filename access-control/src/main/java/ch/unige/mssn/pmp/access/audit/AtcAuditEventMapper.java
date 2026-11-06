package ch.unige.mssn.pmp.access.audit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exprime le journal sous la forme d'AuditEvent du guide CH:ATC. Les systèmes et codes reprennent les exemples du guide ;
 * le code de refus et le texte du motif sont un ajout du projet, CH:ATC ne décrivant que des opérations effectuées.
 */
public final class AtcAuditEventMapper {

    private static final String DOCUMENT_PROFILE = "http://fhir.ch/ig/ch-atc/StructureDefinition/DocumentAuditEvent";
    private static final String LOG_PROFILE = "http://fhir.ch/ig/ch-atc/StructureDefinition/PatientAuditRecordAccessAuditTrailEvent";
    private static final String EVENT_SYSTEM = "urn:oid:2.16.756.5.30.1.127.3.10.7";
    private static final String PURPOSE_SYSTEM = "urn:oid:2.16.756.5.30.1.127.3.10.5";
    private static final String ROLE_SYSTEM = "urn:oid:2.16.756.5.30.1.127.3.10.6";
    private static final String GLN_SYSTEM = "urn:oid:2.51.1.3";
    private static final String SPID_SYSTEM = "urn:oid:2.16.756.5.30.1.127.3.10.3";

    private AtcAuditEventMapper() {
    }

    public static Map<String, Object> toBundle(List<AuditQueries.Entry> entries) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (AuditQueries.Entry entry : entries) {
            items.add(Map.of("resource", toAuditEvent(entry)));
        }
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "searchset");
        bundle.put("total", entries.size());
        bundle.put("entry", items);
        return bundle;
    }

    public static Map<String, Object> toAuditEvent(AuditQueries.Entry e) {
        String atcEvent = e.atcEvent() == null ? "ATC_DOC_READ" : e.atcEvent();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("resourceType", "AuditEvent");
        event.put("id", e.eventUuid().toString());
        event.put("meta", Map.of("profile", List.of(atcEvent.equals("ATC_LOG_READ") ? LOG_PROFILE : DOCUMENT_PROFILE)));
        event.put("subtype", List.of(Map.of("system", EVENT_SYSTEM, "code", atcEvent)));
        event.put("action", action(atcEvent));
        event.put("recorded", e.occurredAt().toString());
        event.put("outcome", e.outcome().equals("DENY") ? "4" : "0");
        event.put("outcomeDesc", e.reason());
        event.put("purposeOfEvent", List.of(Map.of("coding", List.of(Map.of("system", PURPOSE_SYSTEM, "code", e.purposeOfUse())))));
        event.put("agent", List.of(agent(e)));
        event.put("entity", List.of(patient(e)));
        return event;
    }

    private static String action(String atcEvent) {
        return switch (atcEvent) {
            case "ATC_DOC_CREATE" -> "C";
            case "ATC_DOC_UPDATE" -> "U";
            default -> "R";
        };
    }

    private static Map<String, Object> agent(AuditQueries.Entry e) {
        Map<String, Object> agent = new LinkedHashMap<>();
        if (e.actingRole() != null) {
            agent.put("role", List.of(Map.of("coding", List.of(Map.of("system", ROLE_SYSTEM, "code", atcRole(e.actingRole()))))));
        }
        agent.put("who", Map.of("identifier", Map.of("system", GLN_SYSTEM, "value", e.personId())));
        if (e.personName() != null) {
            agent.put("name", e.personName());
        }
        agent.put("requestor", true);
        return agent;
    }

    private static String atcRole(String role) {
        return switch (role) {
            case "PATIENT" -> "PAT";
            case "REPRESENTANT" -> "REP";
            case "AUDITEUR" -> "PADM";
            default -> "HCP";
        };
    }

    private static Map<String, Object> patient(AuditQueries.Entry e) {
        Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("what", Map.of("identifier", Map.of("system", SPID_SYSTEM, "value", String.valueOf(e.patientId()))));
        entity.put("type", Map.of("system", "http://terminology.hl7.org/CodeSystem/audit-entity-type", "code", "1", "display", "Person"));
        entity.put("role", Map.of("system", "http://terminology.hl7.org/CodeSystem/object-role", "code", "1", "display", "Patient"));
        return entity;
    }
}
