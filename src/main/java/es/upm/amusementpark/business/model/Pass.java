package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A personal extra bought for one individual ticket. Its price is fixed at
 * purchase and it stays in the system after being consumed, as a record of
 * the purchase.
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

    public abstract PassType getType();

    /**
     * Marks the pass as used.
     *
     * @throws IllegalStateException if it was already consumed
     */
    public void consume() {
        if (consumed) {
            throw new IllegalStateException("Pass " + id + " is already consumed");
        }
        consumed = true;
    }

    /** Undoes {@link #consume()} when the entry it was used for could not be completed. */
    public void revertConsumption() {
        consumed = false;
    }

    /** Only unconsumed passes of tickets dated strictly after today can be refunded. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }

    /** {@code percent} of the ticket's stored price, rounded half-up to cents. */
    protected static BigDecimal shareOf(IndividualTicket ticket, int percent) {
        return ticket.getPrice().multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2,
                RoundingMode.HALF_UP);
    }
}
