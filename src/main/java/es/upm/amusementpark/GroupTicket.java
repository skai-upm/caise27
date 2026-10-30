package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase time.
 * The composition never changes. The group reference becomes null if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /**
     * Discount percentage by number of members: fewer than 3 → 0, 3-5 → 10, 6-10 → 20, more than 10 → 50.
     */
    public static int discountFor(int members) {
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

    /**
     * Identifier of the composed ticket for a member.
     */
    public static String composedId(String groupTicketId, User user) {
        return groupTicketId + ":" + user.getDni();
    }

    public Group getGroup() {
        return group;
    }

    void detachGroup() {
        this.group = null;
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : composition) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            holders.add(ticket.getUser());
        }
        return holders;
    }
}
