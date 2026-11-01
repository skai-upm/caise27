package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pass that lets its ticket holder use the fast lane once. It costs 20% of the
 * price stored in its ticket.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(Ticket ticket) {
        return ticket.getPrice().multiply(PRICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
