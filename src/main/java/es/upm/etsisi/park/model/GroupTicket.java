package es.upm.etsisi.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase
 * time. The composition never changes; the group reference is cleared if the group is
 * deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> tickets) {
        super(id, date);
        this.group = group;
        this.tickets = List.copyOf(tickets);
    }

    /** The purchasing group, or null once it has been deleted. */
    public Group getGroup() {
        return group;
    }

    public void detachGroup() {
        this.group = null;
    }

    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (IndividualTicket ticket : tickets) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            users.add(ticket.getUser());
        }
        return users;
    }
}
