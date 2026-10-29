package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A joint ticket composed of one individual ticket per group member at purchase time.
 * The composition never changes; the group reference becomes null if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> composition;

    /**
     * Builds the composition from the given members, deriving each individual ticket id as
     * {@code <id>:<dni>}.
     */
    public GroupTicket(String id, LocalDate date, Group group, List<User> members, BigDecimal basePrice) {
        this(id, date, group, members, basePrice, discountFor(members.size()));
    }

    GroupTicket(String id, LocalDate date, Group group, List<User> members, BigDecimal basePrice,
                int discountPercent) {
        super(id, date);
        this.group = group;
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(memberTicketId(id, member), date, member, basePrice, discountPercent));
        }
        this.composition = List.copyOf(tickets);
    }

    public static String memberTicketId(String groupTicketId, User member) {
        return groupTicketId + ":" + member.getDni();
    }

    /** Discount percentage by number of members included in the purchase. */
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
