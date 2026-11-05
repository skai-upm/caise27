package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A personal purchase associated with an individual ticket. A pass knows its ticket; the ticket does not know
 * its passes. A consumed pass stays stored as a record of the purchase.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, BigDecimal price) {
        this.id = Objects.requireNonNull(id);
        this.ticket = Objects.requireNonNull(ticket);
        this.price = price.setScale(2, RoundingMode.HALF_UP);
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

    /** Marks the pass as used; it remains associated with its ticket. */
    public void consume() {
        consumed = true;
    }

    /** A pass can be refunded only while unconsumed and when its ticket is dated strictly after the given date. */
    public boolean isRefundableOn(LocalDate currentDate) {
        return !consumed && ticket.getDate().isAfter(currentDate);
    }

    /** Human-readable name of the pass type, for example "SpeedPass". */
    public abstract String typeName();
}
