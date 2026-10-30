package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions. Costs the same as its individual ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static SpecialPass purchase(String id, IndividualTicket ticket) {
        return new SpecialPass(id, ticket, ticket.getPrice(), false);
    }
}
