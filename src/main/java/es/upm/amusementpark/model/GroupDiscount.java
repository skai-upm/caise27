package es.upm.amusementpark.model;

/**
 * Discount brackets for group tickets, by number of members included.
 */
public enum GroupDiscount {
    NONE(1, 2, 0),
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

    public int getPercent() {
        return percent;
    }

    public static GroupDiscount forMembers(int members) {
        for (GroupDiscount bracket : values()) {
            if (members >= bracket.minMembers && members <= bracket.maxMembers) {
                return bracket;
            }
        }
        return NONE;
    }
}
