package es.upm.amusementpark.business;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SpeedPass: costs 20% of the associated individual ticket price (fixed at purchase).
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket));
    }

    /** Rebuilds a persisted SpeedPass with its stored price. */
    public SpeedPass(String id, IndividualTicket ticket, BigDecimal storedPrice) {
        super(id, ticket, storedPrice);
    }

    /** @return the SpeedPass price for a ticket */
    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getTypeName() {
        return "SpeedPass";
    }
}
