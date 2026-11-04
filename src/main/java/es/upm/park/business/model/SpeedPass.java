package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A SpeedPass: costs 20% of its ticket's stored price. */
public class SpeedPass extends Pass {

    public static final String KIND = "SPEED";
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket, String ticketId, BigDecimal price, boolean consumed) {
        super(id, ticket, ticketId, price, consumed);
    }

    /** Price of a SpeedPass for {@code ticket}. */
    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return KIND;
    }
}
