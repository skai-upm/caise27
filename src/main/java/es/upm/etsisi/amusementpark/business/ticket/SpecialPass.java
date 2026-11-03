package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions. It costs the same as its ticket and a
 * ticket may have at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }

    /** Creates a new, unconsumed Special Pass priced from the ticket. */
    public static SpecialPass purchase(String identifier, IndividualTicket ticket) {
        return new SpecialPass(identifier, ticket, ticket.getPrice(), false);
    }
}
