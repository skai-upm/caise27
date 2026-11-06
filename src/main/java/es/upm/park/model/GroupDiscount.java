package es.upm.park.model;

import java.math.BigDecimal;

/**
 * Discount applied to each ticket of a group purchase depending on how many
 * members it includes.
 */
public final class GroupDiscount {

    private static final BigDecimal TEN_PERCENT = new BigDecimal("0.10");
    private static final BigDecimal TWENTY_PERCENT = new BigDecimal("0.20");
    private static final BigDecimal FIFTY_PERCENT = new BigDecimal("0.50");

    private GroupDiscount() {
    }

    /** Fewer than 3: 0%; 3-5: 10%; 6-10: 20%; more than 10: 50%. */
    public static BigDecimal forMembers(int members) {
        if (members < 3) {
            return BigDecimal.ZERO;
        }
        if (members <= 5) {
            return TEN_PERCENT;
        }
        if (members <= 10) {
            return TWENTY_PERCENT;
        }
        return FIFTY_PERCENT;
    }
}
