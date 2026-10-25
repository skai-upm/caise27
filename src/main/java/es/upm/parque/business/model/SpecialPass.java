package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * The pass required by SPECIAL attractions; it costs the same as its ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    public static BigDecimal priceFor(IndividualTicket ticket) {
        return ticket.getPrice();
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
