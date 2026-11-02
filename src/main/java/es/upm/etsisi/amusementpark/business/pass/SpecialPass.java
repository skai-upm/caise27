package es.upm.etsisi.amusementpark.business.pass;

import java.math.BigDecimal;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * Admission to a SPECIAL attraction. Costs its ticket's stored price; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String identifier, IndividualTicket ticket) {
        this(identifier, ticket, ticket.getPrice(), false);
    }

    public SpecialPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }
}
