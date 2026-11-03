package es.upm.etsisi.park.business;

import java.math.BigDecimal;

/**
 * Personal extra purchased for one individual ticket. A consumed pass stays
 * stored as a purchase record.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.id = id;
        this.ticket = ticket;
        this.price = price;
        this.consumed = consumed;
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

    void markConsumed() {
        this.consumed = true;
    }

    void markUnconsumed() {
        this.consumed = false;
    }
}
