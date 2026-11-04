package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A Special Pass: the same price as its ticket; at most one per ticket. */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, priceFor(ticket.getPrice()), ticket, false);
    }

    /** Restores a stored Special Pass. */
    public SpecialPass(String id, BigDecimal price, IndividualTicket ticket, boolean consumed) {
        super(id, price, ticket, consumed);
    }

    public static BigDecimal priceFor(BigDecimal ticketPrice) {
        return ticketPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String kind() {
        return "SpecialPass";
    }
}
