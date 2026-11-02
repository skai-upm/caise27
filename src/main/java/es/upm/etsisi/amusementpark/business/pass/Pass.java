package es.upm.etsisi.amusementpark.business.pass;

import java.math.BigDecimal;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * A personal extra bought for one individual ticket. Its price is fixed at purchase;
 * once consumed it stays as a purchase record. The ticket does not reference its passes.
 */
public abstract class Pass {
    private final String identifier;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.identifier = identifier;
        this.ticket = ticket;
        this.price = price;
        this.consumed = consumed;
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

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
