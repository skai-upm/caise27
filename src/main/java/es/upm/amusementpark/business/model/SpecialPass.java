package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass that costs the same as its individual ticket. A ticket has at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, false);
    }

    public SpecialPass(String id, IndividualTicket ticket, boolean consumed) {
        super(id, ticket, consumed);
    }

    @Override
    public BigDecimal getPrice() {
        return getTicket().getPrice();
    }
}
