package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pass that costs 20% of its individual ticket's price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    /**
     * New unconsumed SpeedPass priced from its ticket.
     */
    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, ticket.getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP), false);
    }

    /**
     * SpeedPass rebuilt from stored data.
     */
    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    /**
     * Identifier of the {@code number}-th pass of a purchase: {@code <purchaseIdentifier>:<number>}.
     */
    public static String derivedId(String purchaseId, int number) {
        return purchaseId + ":" + number;
    }
}
