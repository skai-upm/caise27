package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A special pass, costing the same as its ticket. A ticket has at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String identifier, IndividualTicket ticket) {
        super(identifier, ticket, priceFor(ticket));
    }

    /** Restores a stored Special Pass with the price it was bought for. */
    public SpecialPass(String identifier, IndividualTicket ticket, BigDecimal storedPrice) {
        super(identifier, ticket, storedPrice);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }
}
