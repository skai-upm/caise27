package es.upm.etsisi.amusementpark.business.pass;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.math.BigDecimal;

/**
 * A personal pass bought for one individual ticket. The pass knows its ticket; tickets do not
 * know their passes. Consumed passes are kept as purchase records.
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

    /** Reverts a consumption that could not be persisted. */
    public void restoreUnconsumed() {
        consumed = false;
    }
}
