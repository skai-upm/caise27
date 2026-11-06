package es.upm.park.business.model;

import java.math.BigDecimal;

/**
 * A SpeedPass: costs 20% of its ticket's price. A ticket may have any number.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal FACTOR = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        super(id, ticket);
    }

    private SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** Rebuilds a stored SpeedPass. */
    public static SpeedPass restore(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        return new SpeedPass(id, ticket, price, consumed);
    }

    @Override
    protected BigDecimal priceFactor() {
        return FACTOR;
    }
}
