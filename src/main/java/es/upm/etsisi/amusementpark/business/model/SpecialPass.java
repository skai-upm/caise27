package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter special attractions; costs the stored price of its individual ticket.
 * An individual ticket holds at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static SpecialPass purchase(String id, IndividualTicket ticket) {
        return new SpecialPass(id, ticket, ticket.getPrice(), false);
    }
}
