package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.model.AccessDecision;
import java.util.UUID;

public class AccessRefusedException extends RuntimeException {

    private final transient AccessDecision decision;
    private final UUID eventUuid;

    public AccessRefusedException(AccessDecision decision, UUID eventUuid) {
        super(decision.reason().name());
        this.decision = decision;
        this.eventUuid = eventUuid;
    }

    public AccessDecision decision() {
        return decision;
    }

    public UUID eventUuid() {
        return eventUuid;
    }
}
