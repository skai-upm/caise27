package es.upm.etsisi.park.business.model.pass;

import es.upm.etsisi.park.business.model.IndividualTicket;

import java.math.BigDecimal;

/**
 * Entry to a special attraction. Costs the same as its ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** A new, unconsumed Special Pass priced as the ticket. */
    public static SpecialPass purchase(String id, IndividualTicket ticket) {
        return new SpecialPass(id, ticket, ticket.getPrice(), false);
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
