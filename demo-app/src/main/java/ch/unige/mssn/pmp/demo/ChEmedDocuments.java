package ch.unige.mssn.pmp.demo;

import ch.unige.mssn.pmp.access.model.Confidentiality;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Produit les documents CH EMED EPR à partir des gabarits dérivés des exemples officiels du guide. */
@Component
public class ChEmedDocuments {

    private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");

    public enum Type { MTP, PRE, DIS, PADV }

    public record Author(String gln, String family, String given) {
        static Author of(String gln, String displayName) {
            String name = displayName == null ? "Inconnu" : displayName.strip();
            int space = name.lastIndexOf(' ');
            return space < 0 ? new Author(gln, name, "-") : new Author(gln, name.substring(space + 1), name.substring(0, space));
        }
    }

    public record Patient(String id, String family, String given, String gender, String birthDate) {}

    public record Content(Type type, String documentUuid, String itemUuid, String treatmentUuid, String treatmentDocumentUuid,
                          Patient patient, Author author, Confidentiality level, String atc, String medication, String dosage,
                          String adviceCode, String note, Instant at) {}

    private final Map<Type, String> templates = new HashMap<>();

    public String render(Content c) {
        String text = templates.computeIfAbsent(c.type(), ChEmedDocuments::load);
        Map<String, String> values = new HashMap<>();
        values.put("DOCUMENT_UUID", "urn:uuid:" + c.documentUuid());
        values.put("ITEM_UUID", "urn:uuid:" + c.itemUuid());
        values.put("TREATMENT_UUID", "urn:uuid:" + c.treatmentUuid());
        values.put("TREATMENT_DOCUMENT_UUID", "urn:uuid:" + c.treatmentDocumentUuid());
        values.put("PRESCRIPTION_UUID", "urn:uuid:" + c.treatmentUuid());
        values.put("PRESCRIPTION_DOCUMENT_UUID", "urn:uuid:" + c.treatmentDocumentUuid());
        values.put("TIMESTAMP", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(c.at().truncatedTo(ChronoUnit.SECONDS).atZone(ZURICH)));
        values.put("DATE", DateTimeFormatter.ISO_LOCAL_DATE.format(c.at().atZone(ZURICH)));
        values.put("PATIENT_ID", c.patient().id());
        values.put("PATIENT_FAMILY", c.patient().family());
        values.put("PATIENT_GIVEN", c.patient().given());
        values.put("PATIENT_GENDER", c.patient().gender());
        values.put("PATIENT_BIRTHDATE", c.patient().birthDate());
        values.put("AUTHOR_GLN", c.author().gln());
        values.put("AUTHOR_FAMILY", c.author().family());
        values.put("AUTHOR_GIVEN", c.author().given());
        values.put("ATC", c.atc());
        values.put("MEDICATION", c.medication());
        values.put("DOSAGE", c.dosage());
        values.put("ADVICE_CODE", c.adviceCode());
        values.put("NOTE", c.note());
        values.put("CONF_CODE", switch (c.level()) { case NORMAL -> "N"; case RESTREINT -> "R"; case SECRET -> "V"; });
        values.put("CONF_SNOMED", switch (c.level()) { case NORMAL -> "17621005"; case RESTREINT -> "263856008"; case SECRET -> "1141000195107"; });
        values.put("CONF_DISPLAY", switch (c.level()) {
            case NORMAL -> "Normal (qualifier value)";
            case RESTREINT -> "Restricted (qualifier value)";
            case SECRET -> "Secret (qualifier value)";
        });
        for (Map.Entry<String, String> value : values.entrySet()) {
            text = text.replace("{{" + value.getKey() + "}}", escape(value.getValue()));
        }
        return text;
    }

    private static String load(Type type) {
        try {
            return new ClassPathResource("fhir/" + type.name().toLowerCase() + "-template.json").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Échappe une valeur pour l'insérer dans une chaîne JSON du gabarit. */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        value.codePoints().forEach(c -> {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", c));
                    } else {
                        out.appendCodePoint(c);
                    }
                }
            }
        });
        return out.toString();
    }
}
