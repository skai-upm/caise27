package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Personal add-on bought for an individual ticket. Consumed passes are kept as purchase records.
 */
public abstract class Pass {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final String id;
    private final BigDecimal price;
    private final IndividualTicket ticket;
    private boolean consumed;

    protected Pass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        this.id = id;
        this.price = price;
        this.ticket = ticket;
        this.consumed = consumed;
    }

    public String getId() {
        return id;
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
        if (consumed) {
            throw new OperationNotAllowedException("Pass " + id + " has already been consumed");
        }
        consumed = true;
    }

    /**
     * Only unconsumed passes of tickets dated strictly after the current date can be refunded.
     */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }

    public abstract PassType getType();

    protected static BigDecimal percentOf(BigDecimal amount, int percent) {
        return amount.multiply(BigDecimal.valueOf(percent)).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
