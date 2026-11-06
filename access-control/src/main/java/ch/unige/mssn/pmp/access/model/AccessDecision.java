package ch.unige.mssn.pmp.access.model;

/**
 * Résultat d'une décision.
 *
 * @param effectiveRole rôle au titre duquel la décision est prise, {@code null} s'il n'y en a aucun
 * @param clearance     niveau le plus élevé que le demandeur peut voir ; {@code null} s'il ne voit aucune donnée clinique
 */
public record AccessDecision(Outcome outcome, ReasonCode reason, Role effectiveRole, Confidentiality clearance) {

    public static AccessDecision deny(ReasonCode reason, Role role) {
        return new AccessDecision(Outcome.DENY, reason, role, null);
    }

    public static AccessDecision permit(ReasonCode reason, Role role, Confidentiality clearance) {
        return new AccessDecision(Outcome.PERMIT, reason, role, clearance);
    }

    public static AccessDecision emergency(Role role, Confidentiality clearance) {
        return new AccessDecision(Outcome.EMERGENCY_PERMIT, ReasonCode.EMERGENCY_ACCESS, role, clearance);
    }

    public boolean permitted() {
        return outcome.permitted();
    }
}
