package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * The pass required by special attractions. It costs the same as its ticket; a ticket has at most one.
 */
public class SpecialPass extends Pass {

    public static final String TYPE = "SPECIALPASS";

    /**
     * Creates a new, unused Special Pass priced from its ticket.
     */
    public SpecialPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice(), false);
    }

    /**
     * Rebuilds a stored Special Pass.
     */
    public SpecialPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
