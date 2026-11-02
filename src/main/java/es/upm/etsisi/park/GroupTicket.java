package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Joint ticket of a group, composed of one individual ticket per member at purchase time.
 * The composition never changes; the group reference is cleared if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private static final int SMALL_GROUP_LIMIT = 3;
    private static final int MEDIUM_GROUP_LIMIT = 5;
    private static final int LARGE_GROUP_LIMIT = 10;

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /** Discount percentage for a purchase including the given number of members. */
    public static int discountFor(int members) {
        if (members < SMALL_GROUP_LIMIT) {
            return 0;
        }
        if (members <= MEDIUM_GROUP_LIMIT) {
            return 10;
        }
        if (members <= LARGE_GROUP_LIMIT) {
            return 20;
        }
        return 50;
    }

    /** Identifier of the constituent ticket for the member with the given DNI. */
    public static String componentId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    public Group getGroup() {
        return group;
    }

    void clearGroup() {
        this.group = null;
    }

    void restoreGroup(Group group) {
        this.group = group;
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (IndividualTicket t : composition) {
            total = total.add(t.getPrice());
        }
        return Money.of(total);
    }

    @Override
    public List<User> getHolders() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }
}
