package ch.unige.mssn.pmp.access.spring;

public class AuditUnavailableException extends RuntimeException {

    public AuditUnavailableException(Throwable cause) {
        super("Journal d'audit indisponible", cause);
    }
}
