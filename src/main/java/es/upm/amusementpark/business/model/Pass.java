package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A personal pass associated with an individual ticket. Consumed passes stay
 * in the system as purchase records.
 */
public abstract class Pass {

    private final String identifier;
    private final BigDecimal price;
    private final IndividualTicket ticket;
    private boolean consumed;

    protected Pass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.identifier = Objects.requireNonNull(identifier);
        this.ticket = Objects.requireNonNull(ticket);
        this.price = Objects.requireNonNull(price);
        this.consumed = consumed;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public IndividualTicket getTicket() {
        return ticket;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

    /** A pass can be refunded while unconsumed and its ticket is dated after the given day. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.getDate().isAfter(today);
    }
}
