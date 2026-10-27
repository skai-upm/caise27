package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A pass for faster access, costing 20% of its ticket's price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String identifier, IndividualTicket ticket) {
        super(identifier, ticket, priceFor(ticket));
    }

    /** Restores a stored SpeedPass with the price it was bought for. */
    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal storedPrice) {
        super(identifier, ticket, storedPrice);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
