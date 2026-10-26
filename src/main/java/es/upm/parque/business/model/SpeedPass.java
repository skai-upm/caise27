package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-access pass. Its price is 20% of the price stored in its individual ticket.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    /**
     * Price of a SpeedPass for a ticket bought at the given price.
     */
    public static BigDecimal priceFor(BigDecimal ticketPrice) {
        return ticketPrice.multiply(PRICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
