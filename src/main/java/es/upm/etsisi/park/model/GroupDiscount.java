package es.upm.etsisi.park.model;

/**
 * Discount brackets for group tickets, by number of members included in the purchase.
 */
public final class GroupDiscount {

    private GroupDiscount() {
    }

    public static int percentFor(int members) {
        if (members < 3) {
            return 0;
        }
        if (members <= 5) {
            return 10;
        }
        if (members <= 10) {
            return 20;
        }
        return 50;
    }
}
