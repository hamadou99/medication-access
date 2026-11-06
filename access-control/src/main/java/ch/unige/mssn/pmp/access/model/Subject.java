package ch.unige.mssn.pmp.access.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Personne identifiée par le jeton.
 *
 * @param personId  identifiant de la personne (GLN pour un professionnel)
 * @param patientId identifiant de son propre dossier s'il en a un, sinon {@code null}
 */
public record Subject(String personId, String patientId, String displayName, Set<Role> roles) {

    public Subject {
        roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public boolean isSelf(String targetPatientId) {
        return patientId != null && patientId.equals(targetPatientId);
    }
}
