package es.upm.etsisi.park.business;

import java.math.BigDecimal;

/**
 * Priority-entry pass; costs 20% of its ticket's price. A ticket may have any number of them.
 */
public class SpeedPass extends Pass {

    static final int PRICE_PERCENT = 20;

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    static BigDecimal priceFor(IndividualTicket ticket) {
        return Money.discounted(ticket.getPrice(), 100 - PRICE_PERCENT);
    }
}
