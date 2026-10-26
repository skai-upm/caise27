package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-access pass. It costs 20% of the price stored in its individual ticket.
 */
public class SpeedPass extends Pass {
    private static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(PRICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
