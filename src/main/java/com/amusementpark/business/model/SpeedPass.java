package com.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-entry pass; an individual ticket may have many.
 */
public class SpeedPass extends Pass {
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    /**
     * Price of a SpeedPass for a ticket with the given price: 20% of it.
     */
    public static BigDecimal priceFor(BigDecimal ticketPrice) {
        return ticketPrice.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
