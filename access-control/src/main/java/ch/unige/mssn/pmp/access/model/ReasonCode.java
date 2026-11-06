package ch.unige.mssn.pmp.access.model;

/**
 * Motif d'une décision. {@code journalText} est écrit dans le journal ; {@code publicMessage} est renvoyé
 * au demandeur et reste volontairement général lorsque le détail révélerait quelque chose sur le patient
 * (existence du dossier, exclusion décidée par le patient).
 */
public enum ReasonCode {
    SELF_ACCESS("Accès du patient à ses propres données", null),
    CARE_RELATIONSHIP("Relation de soins établie", null),
    VALID_MANDATE("Mandat de représentation valide", null),
    OVERSIGHT_ROLE("Contrôle par la fonction de surveillance", null),
    EMERGENCY_ACCESS("Accès d'urgence (bris de glace), à vérifier", null),

    NO_RELATIONSHIP("Aucune relation de soins ni mandat avec ce patient", Messages.NO_RIGHT),
    PATIENT_UNKNOWN("Patient inconnu", Messages.NO_RIGHT),
    EXCLUDED_BY_PATIENT("Professionnel exclu par le patient", Messages.NO_RIGHT),
    ROLE_NOT_ALLOWED("Le rôle ne permet pas cette action", "Votre rôle ne permet pas cette action."),
    CONSENT_REVOKED("Consentement du patient révoqué", Messages.NO_RIGHT),
    MANDATE_INVALID("Mandat de représentation expiré, révoqué ou pas encore valide",
            "Votre mandat de représentation n'est pas valide à la date de la demande."),
    MANDATE_SCOPE("Le mandat ne couvre pas cette action", "Votre mandat ne couvre pas cette action."),
    GRANT_EXPIRED("Droit d'accès expiré ou pas encore valide",
            "Votre droit d'accès à ce dossier n'est pas valide à la date de la demande."),
    CONFIDENTIALITY_TOO_HIGH("Niveau de confidentialité supérieur au droit accordé",
            "Votre droit d'accès ne couvre pas le niveau de confidentialité de cette donnée."),
    EMERGENCY_NOT_FOR_ACTION("L'accès d'urgence ne couvre pas cette action",
            "L'accès d'urgence permet uniquement de consulter."),
    EMERGENCY_JUSTIFICATION_MISSING("Justification d'urgence absente ou trop courte",
            "L'accès d'urgence exige une justification d'au moins 10 caractères."),
    EMERGENCY_UNAVAILABLE("Accès d'urgence fermé pour ce dossier", "L'accès d'urgence n'est pas disponible pour ce dossier."),
    UNKNOWN_ACTION("Action absente du tableau de règles", "Cette action n'est pas autorisée."),
    INVALID_SUBJECT("Jeton sans identité ou rôle exploitable", "Votre identité ne permet pas de décider de cet accès."),
    DECISION_ERROR("Erreur lors de la décision : refus par défaut", "L'accès ne peut pas être vérifié pour le moment.");

    private static final class Messages {
        static final String NO_RIGHT = "Vous n'avez pas de droit d'accès à ce dossier. En cas d'urgence médicale, utilisez l'accès d'urgence.";
    }

    private final String journalText;
    private final String publicMessage;

    ReasonCode(String journalText, String publicMessage) {
        this.journalText = journalText;
        this.publicMessage = publicMessage;
    }

    public String journalText() {
        return journalText;
    }

    public String publicMessage() {
        return publicMessage;
    }
}
