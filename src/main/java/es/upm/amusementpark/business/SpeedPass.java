package es.upm.amusementpark.business;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SpeedPass: costs 20% of the associated individual ticket price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        super(id, ticket);
    }

    @Override
    public BigDecimal getPrice() {
        return getTicket().getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getTypeName() {
        return "SpeedPass";
    }
}
