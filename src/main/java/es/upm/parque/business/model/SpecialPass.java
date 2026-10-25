package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pass required to enter SPECIAL attractions. It costs the same as its ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
