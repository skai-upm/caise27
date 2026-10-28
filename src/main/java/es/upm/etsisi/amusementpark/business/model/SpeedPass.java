package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A SpeedPass: costs 20% of the associated individual ticket's price.
 */
public class SpeedPass extends Pass {

    public static final int PERCENTAGE = 20;

    public SpeedPass(String identifier, IndividualTicket ticket) {
        super(identifier, ticket, priceFor(ticket, PERCENTAGE), false);
    }

    /** Rebuilds a stored SpeedPass. */
    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }
}
