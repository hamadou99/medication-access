package ch.unige.mssn.pmp.access.model;

public enum Outcome {
    PERMIT, DENY, EMERGENCY_PERMIT;

    public boolean permitted() {
        return this != DENY;
    }
}
