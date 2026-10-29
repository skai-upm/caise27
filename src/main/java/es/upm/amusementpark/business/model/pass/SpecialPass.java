package es.upm.amusementpark.business.model.pass;

import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions. It costs the same as its ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
