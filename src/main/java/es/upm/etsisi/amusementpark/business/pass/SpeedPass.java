package es.upm.etsisi.amusementpark.business.pass;

import java.math.BigDecimal;
import java.math.RoundingMode;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * Priority access for one entry. Costs 20% of its ticket's stored price.
 */
public class SpeedPass extends Pass {
    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String identifier, IndividualTicket ticket) {
        this(identifier, ticket, priceFor(ticket), false);
    }

    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
