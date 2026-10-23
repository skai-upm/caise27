package es.upm.etsisi.park.business.model.pass;

import java.math.BigDecimal;

import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Money;

/** Skips the regular queue once; costs 20% of its ticket's price. */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** A new, unconsumed SpeedPass priced from its ticket. */
    public static SpeedPass purchase(String id, IndividualTicket ticket) {
        return new SpeedPass(id, ticket, priceFor(ticket), false);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return Money.of(ticket.getPrice().multiply(RATE));
    }

    /** Identifier of the n-th pass (1-based) of a purchase. */
    public static String passId(String purchaseId, int number) {
        return purchaseId + ":" + number;
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
