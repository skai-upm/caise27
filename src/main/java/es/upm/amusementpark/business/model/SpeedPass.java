package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A SpeedPass; it costs 20 % of its individual ticket's price. */
public class SpeedPass extends Pass {

    private static final BigDecimal SHARE = new BigDecimal("0.20");

    public SpeedPass(String identifier, IndividualTicket ticket) {
        this(identifier, ticket, unitPrice(ticket), false);
    }

    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }

    public static BigDecimal unitPrice(IndividualTicket ticket) {
        return ticket.getPrice().multiply(SHARE).setScale(2, RoundingMode.HALF_UP);
    }
}
