package es.upm.amusementpark.business;

import java.math.BigDecimal;

/**
 * Special Pass: costs the same as the associated individual ticket; at most one per ticket.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        super(id, ticket);
    }

    @Override
    public BigDecimal getPrice() {
        return getTicket().getPrice();
    }

    @Override
    public String getTypeName() {
        return "Special Pass";
    }
}
