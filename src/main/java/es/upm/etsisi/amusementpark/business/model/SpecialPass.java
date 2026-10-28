package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A Special Pass: costs the same as the associated individual ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public static final int PERCENTAGE = 100;

    public SpecialPass(String identifier, IndividualTicket ticket) {
        super(identifier, ticket, priceFor(ticket, PERCENTAGE), false);
    }

    /** Rebuilds a stored Special Pass. */
    public SpecialPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }
}
