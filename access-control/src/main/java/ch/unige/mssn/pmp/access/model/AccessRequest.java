package ch.unige.mssn.pmp.access.model;

import java.time.Instant;

/**
 * Demande à décider.
 *
 * @param patientId              patient visé ; {@code null} pour une action sans patient (contrôle global)
 * @param dataLevel              niveau de la donnée visée, {@code null} si la demande n'en vise pas une en particulier
 * @param emergencyJustification justification saisie, {@code null} hors accès d'urgence
 * @param reference              donnée visée (identifiant de traitement), reprise dans le journal
 */
public record AccessRequest(Subject subject, String patientId, String resource, String action,
                            Confidentiality dataLevel, String emergencyJustification, String reference, Instant at) {

    public boolean emergencyRequested() {
        return emergencyJustification != null;
    }
}
