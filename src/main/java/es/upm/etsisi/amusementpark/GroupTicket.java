package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at
 * purchase time. The composition never changes; the group reference becomes
 * null once the group is deleted.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal NO_DISCOUNT = new BigDecimal("0.00");
    private static final BigDecimal SMALL_GROUP_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal MEDIUM_GROUP_DISCOUNT = new BigDecimal("0.20");
    private static final BigDecimal LARGE_GROUP_DISCOUNT = new BigDecimal("0.50");

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(identifier, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /** Discount applied to each member's ticket depending on the number of members. */
    public static BigDecimal discountFor(int members) {
        if (members < 3) {
            return NO_DISCOUNT;
        }
        if (members <= 5) {
            return SMALL_GROUP_DISCOUNT;
        }
        if (members <= 10) {
            return MEDIUM_GROUP_DISCOUNT;
        }
        return LARGE_GROUP_DISCOUNT;
    }

    /** Identifier of the composed ticket for a member: {@code <groupTicketIdentifier>:<dni>}. */
    public static String memberTicketIdentifier(String groupTicketIdentifier, User member) {
        return groupTicketIdentifier + ":" + member.getDni();
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
