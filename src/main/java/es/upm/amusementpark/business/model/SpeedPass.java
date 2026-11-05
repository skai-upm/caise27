package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * SpeedPass: costs 20% of the price stored in its individual ticket.
 */
public class SpeedPass extends Pass {

    public static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String identifier, IndividualTicket ticket) {
        this(identifier, ticket, ticket.getPrice().multiply(RATE), false);
    }

    /** Rebuilds a stored SpeedPass. */
    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }
}
