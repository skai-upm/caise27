package com.parque.business.model;

import java.math.BigDecimal;

/**
 * SpeedPass: costs 20% of the stored price of its individual ticket. Bought singly or in packs of ten.
 */
public class SpeedPass extends Pass {
    public static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    /** New, unconsumed SpeedPass priced from the ticket. */
    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket, PRICE_RATE), false);
    }

    /** SpeedPass with a stored price and status (restored from persistence). */
    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getKind() {
        return "SPEED";
    }
}
