package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass that costs the same as its individual ticket. A ticket has at most one.
 */
public class SpecialPass extends Pass {

    /**
     * New unconsumed Special Pass priced from its ticket.
     */
    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice(), false);
    }

    /**
     * Special Pass rebuilt from stored data.
     */
    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }
}
