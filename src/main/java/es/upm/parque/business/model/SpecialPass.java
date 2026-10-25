package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions. Costs the price stored in its individual ticket;
 * a ticket has at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public String getKind() {
        return "SPECIAL PASS";
    }
}
