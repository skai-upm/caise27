package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Joint ticket bought by a group, composed of one individual ticket per member at purchase
 * time. It references the purchasing group while that group exists; the group does not know it.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private static final BigDecimal NO_DISCOUNT = BigDecimal.ZERO;
    private static final BigDecimal SMALL_GROUP = new BigDecimal("0.10");
    private static final BigDecimal MEDIUM_GROUP = new BigDecimal("0.20");
    private static final BigDecimal LARGE_GROUP = new BigDecimal("0.50");

    private final List<IndividualTicket> composition;
    private Group group;

    public GroupTicket(String id, LocalDate date, BigDecimal basePrice, Group group, List<User> members) {
        super(id, date, basePrice, discountFor(members.size()));
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(derivedId(id, member), date, basePrice, getDiscountRate(), member));
        }
        this.composition = Collections.unmodifiableList(tickets);
        this.group = group;
    }

    /** Identifier of the individual ticket of a member: {@code <groupTicketId>:<dni>}. */
    public static String derivedId(String groupTicketId, User member) {
        return groupTicketId + ":" + member.getDni();
    }

    /** Discount by number of members: &lt;3 0%, 3-5 10%, 6-10 20%, &gt;10 50%. */
    public static BigDecimal discountFor(int members) {
        if (members < 3) {
            return NO_DISCOUNT;
        }
        if (members <= 5) {
            return SMALL_GROUP;
        }
        if (members <= 10) {
            return MEDIUM_GROUP;
        }
        return LARGE_GROUP;
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    /** Purchasing group, or null once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    public void clearGroup() {
        this.group = null;
    }

    /** Total price: the sum of the discounted prices of its individual tickets. */
    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : composition) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    @Override
    public boolean usesId(String ticketId) {
        if (super.usesId(ticketId)) {
            return true;
        }
        for (IndividualTicket ticket : composition) {
            if (ticket.usesId(ticketId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean involves(User user) {
        for (IndividualTicket ticket : composition) {
            if (ticket.involves(user)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> holders() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            users.add(ticket.getUser());
        }
        return users;
    }
}
