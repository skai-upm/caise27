package es.upm.park.business.model.pass;

import es.upm.park.business.model.IndividualTicket;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions; costs the same as its ticket. A
 * ticket may have at most one.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice(), false);
    }

    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
