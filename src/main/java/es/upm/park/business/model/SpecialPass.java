package es.upm.park.business.model;

import java.math.BigDecimal;

/** A Special Pass: same price as the associated individual ticket; at most one per ticket. */
public class SpecialPass extends Pass {

    public static final int PERCENT = 100;

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return percentOfTicket(ticket, PERCENT);
    }
}
