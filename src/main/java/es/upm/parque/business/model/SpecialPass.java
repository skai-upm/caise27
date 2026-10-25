package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * Pass for special attractions costing the same as its ticket; at most one per ticket.
 */
public class SpecialPass extends PersonalPass {

    public static final String KIND = "SPECIAL";

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public String getKind() {
        return KIND;
    }
}
