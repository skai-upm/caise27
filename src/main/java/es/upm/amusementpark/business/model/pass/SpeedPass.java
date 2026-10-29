package es.upm.amusementpark.business.model.pass;

import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.model.ticket.Ticket;

import java.math.BigDecimal;

/**
 * Priority access pass. Each one costs 20% of its ticket's price; a ticket may have any number.
 */
public class SpeedPass extends Pass {

    private static final int PRICE_PERCENT = 20;

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return Ticket.percentOf(ticket.getPrice(), PRICE_PERCENT);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
