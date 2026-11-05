package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** At most one per individual ticket; costs the ticket's stored price. */
public class SpecialPass extends Pass {

    public static final String TYPE_KEY = "SPECIAL";

    public SpecialPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return TYPE_KEY;
    }
}
