package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority access pass. Each costs 20 % of its ticket's stored price; a ticket
 * may have any number of them.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    public SpeedPass(String identifier, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(identifier, ticket, price, consumed);
    }

    /** Creates a new, unconsumed SpeedPass priced from the ticket. */
    public static SpeedPass purchase(String identifier, IndividualTicket ticket) {
        return new SpeedPass(identifier, ticket, priceFor(ticket), false);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(PRICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
