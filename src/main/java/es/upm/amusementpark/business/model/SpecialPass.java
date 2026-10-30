package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter SPECIAL attractions. Costs the stored price of its ticket; at most one per ticket.
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
