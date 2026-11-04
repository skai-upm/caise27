package es.upm.park.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. It stores its own price and
 * consumption status; consumed passes are kept.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final String ticketId;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, String ticketId, BigDecimal price, boolean consumed) {
        this.id = id;
        this.ticket = ticket;
        this.ticketId = ticketId;
        this.price = price;
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

    /** The associated ticket, or {@code null} if it no longer exists (history). */
    public IndividualTicket getTicket() {
        return ticket;
    }

    public String getTicketId() {
        return ticketId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void markConsumed() {
        consumed = true;
    }

    public boolean isFor(IndividualTicket t) {
        return ticket != null && ticket == t;
    }

    /** Short name of the pass kind, as shown to users and stored in files. */
    public abstract String kind();
}
