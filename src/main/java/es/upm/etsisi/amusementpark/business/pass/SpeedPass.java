package es.upm.etsisi.amusementpark.business.pass;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-entry pass costing 20% of its ticket's stored price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /** Identifier of the n-th pass (1-based) of a purchase. */
    public static String passId(String purchaseId, int number) {
        return purchaseId + ":" + number;
    }
}
