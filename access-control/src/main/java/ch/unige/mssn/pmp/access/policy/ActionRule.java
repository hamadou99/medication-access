package ch.unige.mssn.pmp.access.policy;

import ch.unige.mssn.pmp.access.model.Role;
import java.util.Set;

/**
 * Règle d'une action.
 *
 * @param emergency  le bris de glace peut ouvrir cette action
 * @param global     l'action ne vise pas un patient (contrôle transversal)
 * @param levelBased la réponse est filtrée selon le niveau accordé plutôt que refusée en bloc
 * @param atcEvent   type d'événement CH:ATC correspondant
 */
public record ActionRule(String resource, String action, Set<Role> roles, boolean emergency, boolean global,
                         boolean levelBased, String atcEvent, String chEmedDocument) {

    public boolean allows(Role role) {
        return roles.contains(role);
    }
}
