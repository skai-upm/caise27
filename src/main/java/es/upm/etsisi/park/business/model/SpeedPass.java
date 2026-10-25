package es.upm.etsisi.park.business.model;

import java.math.BigDecimal;

/**
 * Priority access pass. Costs 20 % of its ticket's price; a ticket may have any number of them.
 */
public class SpeedPass extends Pass {

    public static final int PRICE_PERCENT = 20;

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, priceFor(ticket), ticket, false);
    }

    public SpeedPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return percentOf(ticket.getPrice(), PRICE_PERCENT);
    }

    @Override
    public PassType getType() {
        return PassType.SPEED;
    }
}
