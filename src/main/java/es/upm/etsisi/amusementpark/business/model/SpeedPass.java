package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/** A SpeedPass: costs 20% of the price stored in its individual ticket. */
public class SpeedPass extends Pass {

    public static final String TYPE = "SPEED";
    static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, Ticket ticket) {
        super(id, ticket, RATE);
    }

    public SpeedPass(String id, Ticket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
