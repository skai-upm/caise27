package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Joint ticket of a group, composed of one individual ticket per member at purchase time.
 * The composition never changes; the group reference becomes null if the group is deleted.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    public static final String ID_SEPARATOR = ":";

    private Group group;
    private final List<IndividualTicket> composition;

    /**
     * Creates a group ticket for the given members, deriving each composition ticket identifier.
     */
    public GroupTicket(String id, LocalDate date, BigDecimal basePrice, Group group, List<User> members) {
        this(id, date, basePrice, GroupDiscount.percentFor(members.size()), group, members);
    }

    /**
     * Rebuilds a group ticket with a known discount (used when loading persisted data).
     */
    public GroupTicket(String id, LocalDate date, BigDecimal basePrice, int discountPercent, Group group,
                       List<User> members) {
        super(id, date, basePrice, discountPercent);
        this.group = group;
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(componentId(id, member), date, basePrice, discountPercent, member));
        }
        this.composition = List.copyOf(tickets);
    }

    public static String componentId(String groupTicketId, User member) {
        return groupTicketId + ID_SEPARATOR + member.getDni();
    }

    public Group getGroup() {
        return group;
    }

    /**
     * Drops the reference to a deleted group; the historical record is kept.
     */
    public void detachGroup() {
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
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            users.add(ticket.getUser());
        }
        return users;
    }

    @Override
    public Ticket findTicket(String ticketId) {
        if (getId().equals(ticketId)) {
            return this;
        }
        for (IndividualTicket ticket : composition) {
            if (ticket.getId().equals(ticketId)) {
                return ticket;
            }
        }
        return null;
    }

    @Override
    public List<IndividualTicket> entryTickets() {
        return composition;
    }
}
