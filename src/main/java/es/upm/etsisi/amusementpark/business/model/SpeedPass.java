package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Priority access for one entry. Costs 20% of its ticket's price.
 */
public class SpeedPass extends Pass {

    static final int PRICE_PERCENT = 20;

    public SpeedPass(String id, IndividualTicket ticket, BigDecimal price, boolean consumed) {
        super(id, ticket, price, consumed);
    }

    public static SpeedPass purchase(String id, IndividualTicket ticket) {
        return new SpeedPass(id, ticket, unitPrice(ticket), false);
    }

    public static BigDecimal unitPrice(IndividualTicket ticket) {
        return percentOf(ticket, PRICE_PERCENT);
    }

    /**
     * Identifier of the n-th pass (1-based) of a purchase.
     */
    public static String passId(String purchaseId, int n) {
        return purchaseId + ":" + n;
    }
}
