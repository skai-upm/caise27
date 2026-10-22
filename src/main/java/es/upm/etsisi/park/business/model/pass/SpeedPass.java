package es.upm.etsisi.park.business.model.pass;

import es.upm.etsisi.park.business.model.IndividualTicket;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority access for one entry. Costs 20 % of its ticket's price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");
    private static final int PRICE_SCALE = 2;

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** A new, unconsumed SpeedPass priced from the ticket's stored price. */
    public static SpeedPass purchase(String id, IndividualTicket ticket) {
        BigDecimal price = ticket.getPrice().multiply(RATE).setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        return new SpeedPass(id, ticket, price, false);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
