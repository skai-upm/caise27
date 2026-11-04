package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A SpeedPass: 20% of the price stored in its ticket. */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, priceFor(ticket.getPrice()), ticket, false);
    }

    /** Restores a stored SpeedPass. */
    public SpeedPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(BigDecimal ticketPrice) {
        return ticketPrice.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return "SpeedPass";
    }
}
