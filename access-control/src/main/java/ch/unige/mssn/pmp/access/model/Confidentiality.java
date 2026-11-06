package ch.unige.mssn.pmp.access.model;

/** Niveaux de confidentialité de l'art. 1 ODEP, du moins au plus protégé. */
public enum Confidentiality {
    NORMAL, RESTREINT, SECRET;

    public boolean atMost(Confidentiality clearance) {
        return clearance != null && compareTo(clearance) <= 0;
    }
}
