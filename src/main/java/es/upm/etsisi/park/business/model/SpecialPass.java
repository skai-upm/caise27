package es.upm.etsisi.park.business.model;

import java.math.BigDecimal;

/**
 * Pass required by special attractions. Costs the same as its ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, priceFor(ticket), ticket, false);
    }

    public SpecialPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public PassType getType() {
        return PassType.SPECIAL;
    }
}
