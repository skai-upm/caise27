package es.upm.amusementpark.business.model.pass;

import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A personal extra bought for an individual ticket. Its price is fixed at purchase; once consumed it
 * stays as a record of the purchase. The ticket does not know its passes.
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
        this.consumed = true;
    }

    /**
     * Unconsumed passes of tickets dated after today can be refunded.
     */
    public boolean isRefundable(LocalDate today) {
        return !consumed && ticket.isDeletable(today);
    }

    /**
     * Human-readable kind, e.g. "SpeedPass".
     */
    public abstract String getKind();
}
