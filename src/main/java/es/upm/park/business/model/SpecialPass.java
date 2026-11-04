package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A Special Pass: costs the same as its ticket's stored price; at most one per ticket. */
public class SpecialPass extends Pass {

    public static final String KIND = "SPECIAL";

    public SpecialPass(String id, IndividualTicket ticket, String ticketId, BigDecimal price, boolean consumed) {
        super(id, ticket, ticketId, price, consumed);
    }

    /** Price of a Special Pass for {@code ticket}. */
    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return KIND;
    }
}
