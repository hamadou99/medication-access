package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.model.AccessDecision;

/** Décision de la demande en cours, pour que le code métier filtre les données selon le niveau accordé. */
public final class AccessContext {

    private static final ThreadLocal<AccessDecision> CURRENT = new ThreadLocal<>();

    private AccessContext() {
    }

    public static AccessDecision current() {
        AccessDecision decision = CURRENT.get();
        if (decision == null) {
            throw new IllegalStateException("Aucune décision en cours : la méthode n'est pas protégée par @AccessControlled");
        }
        return decision;
    }

    static void set(AccessDecision decision) {
        CURRENT.set(decision);
    }

    static void clear() {
        CURRENT.remove();
    }
}
