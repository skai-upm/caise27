package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A personal pass bought for an individual ticket. Its price is fixed at purchase
 * from the price stored in the ticket. Consumed passes remain as purchase records.
 */
public abstract class Pass {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

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

    /** Price of a pass costing {@code percentage} % of the ticket's stored price. */
    protected static BigDecimal priceFor(IndividualTicket ticket, int percentage) {
        return ticket.getUnitPrice().multiply(BigDecimal.valueOf(percentage)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
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

    /** Only unconsumed passes of tickets dated strictly after {@code today} may be refunded. */
    public boolean isRefundable(LocalDate today) {
        return !consumed && ticket.getDate().isAfter(today);
    }
}
