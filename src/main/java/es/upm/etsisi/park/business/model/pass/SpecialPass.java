package es.upm.etsisi.park.business.model.pass;

import java.math.BigDecimal;

import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Money;

/** Grants one entry to a special attraction; costs the same as its ticket. */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /** A new, unconsumed Special Pass priced from its ticket. */
    public static SpecialPass purchase(String id, IndividualTicket ticket) {
        return new SpecialPass(id, ticket, priceFor(ticket), false);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return Money.of(ticket.getPrice());
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
