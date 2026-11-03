package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/** The Special Pass of an individual ticket: costs the price stored in the ticket; at most one per ticket. */
public class SpecialPass extends Pass {

    public static final String TYPE = "SPECIAL";

    public SpecialPass(String id, Ticket ticket) {
        super(id, ticket, BigDecimal.ONE);
    }

    public SpecialPass(String id, Ticket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
