package es.upm.park.model;

/**
 * Discount applied to each individual ticket of a group ticket, chosen by
 * the number of members included in the purchase.
 */
public enum GroupDiscount {
    NONE(0),
    SMALL_GROUP(10),
    MEDIUM_GROUP(20),
    LARGE_GROUP(50);

    private final int percent;

    GroupDiscount(int percent) {
        this.percent = percent;
    }

    public int getPercent() {
        return percent;
    }

    public static GroupDiscount forMembers(int members) {
        if (members < 3) {
            return NONE;
        }
        if (members <= 5) {
            return SMALL_GROUP;
        }
        if (members <= 10) {
            return MEDIUM_GROUP;
        }
        return LARGE_GROUP;
    }

    public static GroupDiscount fromPercent(int percent) {
        for (GroupDiscount discount : values()) {
            if (discount.percent == percent) {
                return discount;
            }
        }
        throw new IllegalArgumentException("No group discount of " + percent + "%");
    }
}
