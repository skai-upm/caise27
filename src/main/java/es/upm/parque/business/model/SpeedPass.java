package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-access pass costing 20% of its ticket's price.
 */
public class SpeedPass extends PersonalPass {

    public static final String KIND = "SPEED";
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return KIND;
    }
}
