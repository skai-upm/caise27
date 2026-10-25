package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. Consumed passes are kept with their status.
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

    public void consume() {
        consumed = true;
    }

    /** Name of the pass kind as shown to users. */
    public abstract String getKind();
}
