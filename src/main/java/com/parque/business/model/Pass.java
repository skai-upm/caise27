package com.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A personal pass attached to an individual ticket. Each pass is an independent object with its own price and
 * consumption status; consumed passes stay recorded.
 */
public abstract class Pass {
    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.id = id;
        this.ticket = ticket;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.consumed = consumed;
    }

    /** Price of a pass of the given rate for the ticket's stored price. */
    protected static BigDecimal priceFor(IndividualTicket ticket, BigDecimal rate) {
        return ticket.getPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public IndividualTicket getTicket() {
        return ticket;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isConsumed() {
        return consumed;
    }

    /** Marks the pass as used; it remains recorded. */
    public void consume() {
        consumed = true;
    }

    /** Kind written in persistence and messages: {@code SPEED} or {@code SPECIAL}. */
    public abstract String getKind();
}
