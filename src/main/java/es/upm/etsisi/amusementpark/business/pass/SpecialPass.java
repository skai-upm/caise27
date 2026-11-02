package es.upm.etsisi.amusementpark.business.pass;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions, costing the same as its ticket. At most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice(), false);
    }

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }
}
