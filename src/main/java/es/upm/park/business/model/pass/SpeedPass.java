package es.upm.park.business.model.pass;

import es.upm.park.business.model.IndividualTicket;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Priority-queue pass; costs 20% of its ticket's price. A ticket may have
 * any number of SpeedPasses.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal PRICE_RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, priceFor(ticket), false);
    }

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().multiply(PRICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
