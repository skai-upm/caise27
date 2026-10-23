package es.upm.park.business.model.pass;

import es.upm.park.business.model.IndividualTicket;

import java.math.BigDecimal;

/**
 * A personal add-on bought for one individual ticket. The price is fixed at
 * purchase time; a consumed pass stays as a record of the purchase.
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

    public void consume() {
        consumed = true;
    }

    /**
     * Human-readable pass kind, e.g. for invoices and messages.
     */
    public abstract String getKind();
}
