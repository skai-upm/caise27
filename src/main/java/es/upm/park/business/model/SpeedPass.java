package es.upm.park.business.model;

import java.math.BigDecimal;

/** A SpeedPass: 20% of the associated individual ticket's price. */
public class SpeedPass extends Pass {

    public static final int PERCENT = 20;

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return percentOfTicket(ticket, PERCENT);
    }
}
