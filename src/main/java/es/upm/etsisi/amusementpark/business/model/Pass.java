package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to one individual ticket. It keeps the price
 * charged and whether it has been consumed; consumed passes stay recorded.
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

    public void markConsumed() {
        consumed = true;
    }

    /** Human-readable kind, e.g. {@code SpeedPass}. */
    public abstract String kind();
}
