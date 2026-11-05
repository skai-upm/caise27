package es.upm.park.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket, with its own price and consumption status.
 */
public abstract class Pass {

    private final String id;
    private final BigDecimal price;
    private final IndividualTicket ticket;
    private boolean consumed;

    protected Pass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        this.id = id;
        this.price = price;
        this.ticket = ticket;
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public IndividualTicket getTicket() {
        return ticket;
    }

    public boolean isConsumed() {
        return consumed;
    }

    /** Marks the pass as used; the pass itself is kept. */
    public void consume() {
        consumed = true;
    }

    /** Undoes a consumption that could not be recorded. */
    public void restoreUnused() {
        consumed = false;
    }

    /** Persistence/type keyword of the pass kind. */
    public abstract String kind();
}
