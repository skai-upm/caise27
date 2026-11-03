package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A personal pass bought for an individual ticket. The pass references its
 * ticket; consumed passes remain as purchase records.
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

    public void consume() {
        consumed = true;
    }

    /** Reverts a consumption that could not be completed. */
    public void cancelConsumption() {
        consumed = false;
    }

    /** Unconsumed passes of tickets dated strictly after today may be refunded. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }
}
