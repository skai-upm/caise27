package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A Special Pass: costs the price stored in its individual ticket. A ticket may have at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        super(id, ticket, priceFor(ticket));
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public String typeName() {
        return "Special Pass";
    }
}
