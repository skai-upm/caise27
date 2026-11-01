package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group. It refers to the group and owns one individual
 * ticket per member at purchase time.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, BigDecimal price, Group group, List<IndividualTicket> tickets) {
        super(id, date, price);
        this.group = group;
        this.tickets = List.copyOf(tickets);
    }

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            users.add(t.getUser());
        }
        return users;
    }
}
