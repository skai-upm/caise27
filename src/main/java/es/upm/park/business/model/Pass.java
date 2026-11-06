package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A personal extra bought for one individual ticket. The pass refers to its
 * ticket; tickets do not store their passes. Consumed passes stay as records.
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

    /** A pass can be refunded only while unconsumed and for a ticket dated after today. */
    public boolean isRefundableOn(LocalDate today) {
        return !consumed && ticket.isDeletableOn(today);
    }

    /** Common pricing rule: a percentage of the ticket's stored price, rounded to cents. */
    protected static BigDecimal percentOfTicket(IndividualTicket ticket, int percent) {
        return ticket.getPrice().multiply(BigDecimal.valueOf(percent)).movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
