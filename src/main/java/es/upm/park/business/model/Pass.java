package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A personal extra bought for an individual ticket. Its price derives from
 * the price stored in the ticket; a consumed pass stays as a purchase record.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket) {
        this.id = id;
        this.ticket = ticket;
        this.price = ticket.getPrice().multiply(priceFactor()).setScale(2, RoundingMode.HALF_UP);
    }

    /** Fraction of the ticket price that this pass type costs. */
    protected abstract BigDecimal priceFactor();

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

    /** Refundable while unconsumed and its ticket is dated after today. */
    public boolean isRefundable(LocalDate today) {
        return !consumed && ticket.getDate().isAfter(today);
    }
}
