package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pass required by SPECIAL attractions. Costs the same as its ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PassKind getKind() {
        return PassKind.SPECIAL;
    }
}
