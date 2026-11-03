package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority access pass costing 20% of its ticket's price. A ticket may have any number.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
