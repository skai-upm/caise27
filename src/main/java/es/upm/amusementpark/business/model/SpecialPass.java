package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter special attractions. A ticket can have at most one
 * during its whole existence.
 */
public class SpecialPass extends Pass {

    /** A new, unconsumed Special Pass priced like {@code ticket}. */
    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    /** A Special Pass with a stored price and state, as reloaded from persistence. */
    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return shareOf(ticket, 100);
    }

    @Override
    public PassType getType() {
        return PassType.SPECIAL;
    }
}
