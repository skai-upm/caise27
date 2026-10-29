package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/** A Special Pass; it costs the same as its individual ticket. At most one per ticket. */
public class SpecialPass extends Pass {

    public SpecialPass(String identifier, IndividualTicket ticket) {
        this(identifier, ticket, ticket.getPrice(), false);
    }

    public SpecialPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }
}
