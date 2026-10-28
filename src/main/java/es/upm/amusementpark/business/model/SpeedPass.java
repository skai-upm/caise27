package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pass that costs 20% of its individual ticket's price.
 */
public class SpeedPass extends Pass {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    public SpeedPass(String id, IndividualTicket ticket) {
        this(id, ticket, false);
    }

    public SpeedPass(String id, IndividualTicket ticket, boolean consumed) {
        super(id, ticket, consumed);
    }

    /**
     * Identifier of the {@code number}-th pass of a purchase: {@code <purchaseIdentifier>:<number>}.
     */
    public static String derivedId(String purchaseId, int number) {
        return purchaseId + ":" + number;
    }

    @Override
    public BigDecimal getPrice() {
        return getTicket().getPrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
