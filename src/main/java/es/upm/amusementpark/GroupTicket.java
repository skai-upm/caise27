package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group. It owns one individual ticket per member at
 * purchase time; those tickets refer to their users.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, BigDecimal price, Group group,
                       List<IndividualTicket> tickets) {
        super(id, date, price);
        this.group = group;
        this.tickets = new ArrayList<>(tickets);
    }

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            holders.add(ticket.getUser());
        }
        return holders;
    }
}
