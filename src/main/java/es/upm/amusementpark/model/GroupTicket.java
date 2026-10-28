package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A joint ticket bought by a group, composed of one individual ticket per member at purchase time.
 * The composition never changes. The reference to the purchasing group is cleared if the group is
 * deleted; the group does not know its tickets.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal NO_DISCOUNT = BigDecimal.ZERO;
    private static final BigDecimal SMALL_GROUP_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal MEDIUM_GROUP_DISCOUNT = new BigDecimal("0.20");
    private static final BigDecimal LARGE_GROUP_DISCOUNT = new BigDecimal("0.50");

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<User> members, BigDecimal basePrice) {
        super(id, date);
        this.group = group;
        BigDecimal discount = discountFor(members.size());
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(derivedId(id, member.getDni()), date, member, basePrice, discount, true));
        }
        this.composition = List.copyOf(tickets);
    }

    /**
     * Identifier of the individual ticket of a member: {@code <groupTicketIdentifier>:<dni>}.
     */
    public static String derivedId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    /**
     * Discount by number of members: fewer than 3 none, 3-5 10%, 6-10 20%, more than 10 50%.
     */
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

    /**
     * Purchasing group, or {@code null} once that group has been deleted.
     */
    public Group getGroup() {
        return group;
    }

    public void detachGroup() {
        this.group = null;
    }

    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    @Override
    public List<IndividualTicket> individualTickets() {
        return composition;
    }
}
