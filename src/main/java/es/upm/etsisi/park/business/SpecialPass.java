package es.upm.etsisi.park.business;

import java.math.BigDecimal;

/**
 * Pass required by special attractions; costs the same as its ticket. At most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }
}
