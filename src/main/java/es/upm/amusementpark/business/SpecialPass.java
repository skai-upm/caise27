package es.upm.amusementpark.business;

import java.math.BigDecimal;

/**
 * Special Pass: costs the same as the associated individual ticket (fixed at purchase); at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice());
    }

    /** Rebuilds a persisted Special Pass with its stored price. */
    public SpecialPass(String id, IndividualTicket ticket, BigDecimal storedPrice) {
        super(id, ticket, storedPrice);
    }

    @Override
    public String getTypeName() {
        return "Special Pass";
    }
}
