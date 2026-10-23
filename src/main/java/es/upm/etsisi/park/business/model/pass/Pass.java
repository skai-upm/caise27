package es.upm.etsisi.park.business.model.pass;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Money;

/**
 * A personal extra bought for one individual ticket. Its price is fixed at
 * purchase time; once consumed it stays recorded as a purchase.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        this.id = id;
        this.ticket = ticket;
        this.price = Money.of(price);
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
            throw new IllegalStateException("Pass " + id + " is already consumed");
        }
        consumed = true;
    }

    /** Unconsumed passes of tickets dated strictly after today can be refunded. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.getDate().isAfter(today);
    }

    /** Human-readable pass kind, e.g. "SpeedPass". */
    public abstract String getKind();
}
