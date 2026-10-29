package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter SPECIAL attractions; it costs the same as its individual ticket.
 * An individual ticket has at most one.
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
        return "SPECIALPASS";
    }
}
