package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Personal extra bought for an individual ticket. Its price derives from the ticket's
 * stored price; once consumed it stays as a purchase record.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, BigDecimal priceRate) {
        this.id = id;
        this.ticket = ticket;
        this.price = ticket.getPrice().multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
    }

    /** Restores a stored pass with its stored price and consumption state. */
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

    /** Refunds need an unconsumed pass whose ticket is dated strictly after today. */
    public boolean isRefundable(LocalDate today) {
        return !consumed && ticket.getDate().isAfter(today);
    }
}
