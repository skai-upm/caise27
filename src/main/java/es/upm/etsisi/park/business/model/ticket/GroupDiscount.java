package es.upm.etsisi.park.business.model.ticket;

/** Discount brackets applied to each member ticket of a group ticket. */
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

    /** Fewer than 3: 0%; 3 to 5: 10%; 6 to 10: 20%; more than 10: 50%. */
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
}
