package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Discount brackets for group tickets, by number of members included in the purchase.
 */
public enum GroupDiscount {
    NONE(0, 2, 0),
    SMALL(3, 5, 10),
    MEDIUM(6, 10, 20),
    LARGE(11, Integer.MAX_VALUE, 50);

    private final int minMembers;
    private final int maxMembers;
    private final int percent;

    GroupDiscount(int minMembers, int maxMembers, int percent) {
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
        this.percent = percent;
    }

    public static GroupDiscount forMembers(int members) {
        for (GroupDiscount d : values()) {
            if (members >= d.minMembers && members <= d.maxMembers) {
                return d;
            }
        }
        return NONE;
    }

    public int getPercent() {
        return percent;
    }

    /** Applies this discount to a base price, rounded to cents. */
    public BigDecimal apply(BigDecimal basePrice) {
        BigDecimal factor = BigDecimal.valueOf(100 - percent).divide(BigDecimal.valueOf(100));
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
