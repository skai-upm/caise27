package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A joint ticket bought by a group, composed of one individual ticket per
 * member included at purchase time. The composition never changes after
 * purchase. The purchasing group is referenced while it exists.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /**
     * Creates a group ticket for the current members of {@code group}, applying
     * the discount bracket for their number to {@code basePrice}.
     */
    public static GroupTicket purchase(String id, LocalDate date, Group group, BigDecimal basePrice) {
        List<User> members = group.getMembers();
        int percent = GroupDiscount.forMembers(members.size()).getPercent();
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(componentId(id, member), date, member, basePrice, percent));
        }
        return new GroupTicket(id, date, group, tickets);
    }

    /** Identifier of the individual ticket of {@code user} inside a group ticket. */
    public static String componentId(String groupTicketId, User user) {
        return groupTicketId + ":" + user.getDni();
    }

    /** The purchasing group, or {@code null} once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    /** Forgets the purchasing group after it has been deleted. */
    public void detachGroup() {
        group = null;
    }

    /** Undoes {@link #detachGroup()} when deleting the group could not be completed. */
    public void restoreGroup(Group purchaser) {
        if (group == null) {
            group = purchaser;
        }
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    public int getDiscountPercent() {
        return composition.isEmpty() ? 0 : composition.get(0).getDiscountPercent();
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
    public List<IndividualAccess> getEntryTickets() {
        return new ArrayList<>(composition);
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
