package com.parque.business.model;

import java.math.BigDecimal;

/**
 * Special Pass: costs the stored price of its individual ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {
    public static final BigDecimal PRICE_RATE = BigDecimal.ONE;

    /** New, unconsumed Special Pass priced from the ticket. */
    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket, PRICE_RATE), false);
    }

    /** Special Pass with a stored price and status (restored from persistence). */
    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getKind() {
        return "SPECIAL";
    }
}
