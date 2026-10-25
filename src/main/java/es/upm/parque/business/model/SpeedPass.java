package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority access pass. Costs 20% of the price stored in its individual ticket.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "SPEEDPASS";
    }
}
