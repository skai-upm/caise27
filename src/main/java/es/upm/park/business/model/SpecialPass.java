package es.upm.park.business.model;

import java.math.BigDecimal;

/**
 * A Special Pass: costs the same as its ticket. At most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        super(id, ticket);
    }

    private SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** Rebuilds a stored Special Pass. */
    public static SpecialPass restore(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        return new SpecialPass(id, ticket, price, consumed);
    }

    @Override
    protected BigDecimal priceFactor() {
        return BigDecimal.ONE;
    }
}
