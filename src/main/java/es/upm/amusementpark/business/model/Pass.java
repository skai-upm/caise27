package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A personal add-on bought for an individual ticket. The pass knows its ticket; the ticket does not
 * know its passes. Consumed passes remain as purchase records.
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

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    /**
     * Price charged for the pass, fixed at purchase from the price stored in its ticket.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * A pass can be refunded while unconsumed and its ticket is dated after the current date.
     */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }
}
