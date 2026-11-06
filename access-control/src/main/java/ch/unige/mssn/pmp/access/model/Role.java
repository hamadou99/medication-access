package ch.unige.mssn.pmp.access.model;

/** Rôles reconnus dans le jeton. L'ordre fixe l'évaluation lorsqu'une personne en cumule plusieurs. */
public enum Role {
    PATIENT(Category.SELF),
    REPRESENTANT(Category.MANDATED),
    MEDECIN(Category.PROFESSIONAL),
    PHARMACIEN(Category.PROFESSIONAL),
    SOIGNANT(Category.PROFESSIONAL),
    AUDITEUR(Category.OVERSIGHT);

    public enum Category { SELF, MANDATED, PROFESSIONAL, OVERSIGHT }

    private final Category category;

    Role(Category category) {
        this.category = category;
    }

    public Category category() {
        return category;
    }

    public boolean isProfessional() {
        return category == Category.PROFESSIONAL;
    }
}
