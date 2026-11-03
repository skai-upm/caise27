package es.upm.etsisi.park.business;

import java.math.BigDecimal;

/**
 * Pass required by special attractions, costing the same as its ticket. A ticket
 * has at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }
}
