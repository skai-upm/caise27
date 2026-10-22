package es.upm.etsisi.park.business.model.pass;

import es.upm.etsisi.park.business.model.IndividualTicket;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A personal extra bought for an individual ticket. Its price is fixed at purchase and it
 * stays stored after being consumed, as a record of the purchase.
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

    /** Only unconsumed passes of tickets dated strictly after today can be refunded. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }

    /** Human-readable kind, e.g. "SpeedPass". */
    public abstract String getKind();
}
