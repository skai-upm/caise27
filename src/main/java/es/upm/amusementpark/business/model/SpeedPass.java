package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * SpeedPass: costs 20% of its individual ticket's price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        super(id, ticket, RATE);
    }
}
