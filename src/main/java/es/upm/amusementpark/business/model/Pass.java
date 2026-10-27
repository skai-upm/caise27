package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A personal pass bought for an individual ticket. A consumed pass stays in
 * the system as a record of the purchase.
 */
public abstract class Pass {

    private final String identifier;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String identifier, IndividualTicket ticket, BigDecimal price) {
        this.identifier = identifier;
        this.ticket = ticket;
        this.price = price;
    }

    public String getIdentifier() {
        return identifier;
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

    public void consume() {
        this.consumed = true;
    }
}
