package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority access pass. Costs 20% of the price stored in its individual ticket.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static SpeedPass purchase(String id, IndividualTicket ticket) {
        return new SpeedPass(id, ticket, priceFor(ticket), false);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
