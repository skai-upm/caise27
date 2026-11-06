package es.upm.park.business.model;

import java.math.BigDecimal;

/**
 * A Special Pass: costs the same as its ticket. At most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        super(id, ticket);
    }

    @Override
    protected BigDecimal priceFactor() {
        return BigDecimal.ONE;
    }
}
