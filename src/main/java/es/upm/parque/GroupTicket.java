package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one individual ticket per member at purchase time;
 * its price is the discounted total.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, Group group, LocalDate date, BigDecimal price, List<IndividualTicket> tickets) {
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

    public boolean contains(IndividualTicket ticket) {
        return tickets.contains(ticket);
    }

    @Override
    public List<User> getUsers() {
        return tickets.stream().map(IndividualTicket::getUser).toList();
    }

    /**
     * Discount rate applied to the sum of individual prices for a group of the given size.
     */
    public static BigDecimal discountFor(int members) {
        if (members < 3) {
            return BigDecimal.ZERO;
        }
        if (members <= 5) {
            return new BigDecimal("0.10");
        }
        if (members <= 10) {
            return new BigDecimal("0.20");
        }
        return new BigDecimal("0.50");
    }
}
