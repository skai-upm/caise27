package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A personal purchase attached to one individual ticket. Its price is fixed at purchase time and it stays
 * recorded after being consumed.
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
        if (consumed) {
            throw new OperationNotAllowedException("pass " + id + " has already been consumed");
        }
        consumed = true;
    }

    /**
     * A pass can be refunded while unconsumed and while its ticket is dated strictly after the current date.
     */
    public boolean isRefundableOn(LocalDate current) {
        return !consumed && ticket.isDeletableOn(current);
    }
}
