package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A personal pass associated with one individual ticket (bought directly or part of a group ticket).
 * Its price is fixed at purchase from the ticket's stored price; once consumed it stays recorded.
 */
public abstract class Pass {

    private final String id;
    private final Ticket ticket;
    private final BigDecimal price;
    private boolean consumed;

    /** New pass: the price is derived from the ticket's stored price. */
    protected Pass(String id, Ticket ticket, BigDecimal rate) {
        this(id, ticket, ticket.getPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP), false);
    }

    /** Restored pass with its stored price and status. */
    protected Pass(String id, Ticket ticket, BigDecimal price, boolean consumed) {
        this.id = id;
        this.ticket = ticket;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void markConsumed() {
        this.consumed = true;
    }

    /** Persistent type name of the pass. */
    public abstract String getType();
}
