package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A personal pass associated with an individual ticket. The pass knows its ticket; tickets do not know passes.
 * Consumed passes remain as purchase records.
 */
public abstract class Pass {

    private final String identifier;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.identifier = identifier;
        this.ticket = ticket;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
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
        this.consumed = true;
    }

    /** A pass may be refunded when not consumed and its ticket date is strictly after the current date. */
    public boolean isRefundableOn(LocalDate currentDate) {
        return !consumed && ticket.getDate().isAfter(currentDate);
    }
}
