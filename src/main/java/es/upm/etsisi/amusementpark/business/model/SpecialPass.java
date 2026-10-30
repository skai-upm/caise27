package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Required to enter special attractions. At most one per ticket; costs the ticket's price.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static SpecialPass purchase(String id, IndividualTicket ticket) {
        return new SpecialPass(id, ticket, percentOf(ticket, 100), false);
    }
}
