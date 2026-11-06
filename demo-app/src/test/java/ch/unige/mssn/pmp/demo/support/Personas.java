package ch.unige.mssn.pmp.demo.support;

import java.util.Map;
import java.util.Set;

/** Personnes fictives des scénarios. Les identifiants correspondent à db/seed/V100__droits_fictifs.sql. */
public final class Personas {

    public static final String DUPONT = "761337000000000001";
    public static final String MOREL = "761337000000000002";
    public static final String FAVRE = "761337000000000003";
    public static final String ROCHAT = "761337000000000004";
    public static final String MEYER = "761337000000000005";

    public record Persona(String personId, String patientId, String displayName, Set<String> roles) {}

    private static final Map<String, Persona> ALL = Map.ofEntries(
            Map.entry("Mme Dupont", new Persona("PAT-DUPONT", DUPONT, "Jeanne Dupont", Set.of("PATIENT"))),
            Map.entry("M. Morel", new Persona("PAT-MOREL", MOREL, "Paul Morel", Set.of("PATIENT"))),
            Map.entry("Mme Favre", new Persona("PAT-FAVRE", FAVRE, "Anne Favre", Set.of("PATIENT"))),
            Map.entry("M. Rochat", new Persona("PAT-ROCHAT", ROCHAT, "Luc Rochat", Set.of("PATIENT"))),
            Map.entry("Dre Martin", new Persona("7601000000001", null, "Claire Martin", Set.of("MEDECIN"))),
            Map.entry("le pharmacien Gros", new Persona("7601000000002", null, "Pierre Gros", Set.of("PHARMACIEN"))),
            Map.entry("Dr Keller", new Persona("7601000000003", null, "Marc Keller", Set.of("MEDECIN"))),
            Map.entry("le soignant Blanc", new Persona("7601000000004", null, "Luc Blanc", Set.of("SOIGNANT"))),
            Map.entry("Dr Perrin", new Persona("7601000000005", null, "Éric Perrin", Set.of("MEDECIN"))),
            Map.entry("Dre Roy", new Persona("7601000000006", null, "Nina Roy", Set.of("MEDECIN"))),
            Map.entry("l'infirmière Vidal", new Persona("7601000000007", null, "Inès Vidal", Set.of("SOIGNANT"))),
            Map.entry("Dr Meyer", new Persona("7601000000008", MEYER, "Hugo Meyer", Set.of("MEDECIN", "PATIENT"))),
            Map.entry("Thomas Dupont", new Persona("REP-THOMAS-DUPONT", null, "Thomas Dupont", Set.of("REPRESENTANT"))),
            Map.entry("Léa Dupont", new Persona("REP-LEA-DUPONT", null, "Léa Dupont", Set.of("REPRESENTANT"))),
            Map.entry("Marc Morel", new Persona("REP-MARC-MOREL", null, "Marc Morel", Set.of("REPRESENTANT"))),
            Map.entry("Ana Favre", new Persona("REP-ANA-FAVRE", null, "Ana Favre", Set.of("REPRESENTANT"))),
            Map.entry("l'auditrice Roth", new Persona("AUD-ROTH", null, "Sophie Roth", Set.of("AUDITEUR"))));

    private static final Map<String, String> PATIENTS = Map.of(
            "Mme Dupont", DUPONT, "M. Morel", MOREL, "Mme Favre", FAVRE, "M. Rochat", ROCHAT, "Dr Meyer", MEYER,
            "un patient inconnu", "761337999999999999");

    private Personas() {
    }

    public static Persona named(String name) {
        Persona persona = ALL.get(name);
        if (persona == null) {
            throw new IllegalArgumentException("Personne inconnue des scénarios : " + name);
        }
        return persona;
    }

    public static String patientId(String name) {
        String id = PATIENTS.get(name);
        if (id == null) {
            throw new IllegalArgumentException("Patient inconnu des scénarios : " + name);
        }
        return id;
    }
}
