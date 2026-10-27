package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Special Pass: costs the same as its individual ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        super(id, ticket, BigDecimal.ONE);
    }
}
