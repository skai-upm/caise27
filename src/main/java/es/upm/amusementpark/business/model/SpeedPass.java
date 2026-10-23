package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass for faster entry. A ticket may have any number of them; each one is
 * used for a single entry.
 */
public class SpeedPass extends Pass {

    /** Price of a SpeedPass as a percentage of its ticket's price. */
    public static final int PRICE_PERCENT = 20;

    /** A new, unconsumed SpeedPass priced from {@code ticket}. */
    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    /** A SpeedPass with a stored price and state, as reloaded from persistence. */
    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return shareOf(ticket, PRICE_PERCENT);
    }

    @Override
    public PassType getType() {
        return PassType.SPEED;
    }
}
