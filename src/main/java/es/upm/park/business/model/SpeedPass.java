package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pass bought singly or in packs of ten; costs 20% of its ticket's stored price. */
public class SpeedPass extends Pass {

    public static final String TYPE_KEY = "SPEED";
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return TYPE_KEY;
    }
}
