package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A single skip-the-line use. It costs 20% of the price of its ticket.
 */
public class SpeedPass extends Pass {

    public static final String TYPE = "SPEEDPASS";
    private static final BigDecimal RATE = new BigDecimal("0.20");

    /**
     * Creates a new, unused SpeedPass priced from its ticket.
     */
    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    /**
     * Rebuilds a stored SpeedPass.
     */
    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
