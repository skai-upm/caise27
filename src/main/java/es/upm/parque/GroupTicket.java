package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one individual ticket per member at purchase time.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final BigDecimal price;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, Group group, BigDecimal price, List<IndividualTicket> tickets) {
        super(id, date);
        this.group = group;
        this.price = price;
        this.tickets = new ArrayList<>(tickets);
    }

    /**
     * Buys a group ticket: one individual ticket per current member at {@code unitPrice}, with the discount for
     * the member count applied to the total.
     */
    public static GroupTicket purchase(String id, LocalDate date, Group group, BigDecimal unitPrice) {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : group.getMembers()) {
            tickets.add(new IndividualTicket(internalTicketId(id, member), date, member, unitPrice));
        }
        return new GroupTicket(id, date, group, totalFor(unitPrice, tickets.size()), tickets);
    }

    public static String internalTicketId(String groupTicketId, User member) {
        return groupTicketId + "-" + member.getDni();
    }

    /**
     * Discount percentage for a group of the given size.
     */
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

    public static BigDecimal totalFor(BigDecimal unitPrice, int members) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = BigDecimal.valueOf(100 - discountFor(members)).movePointLeft(2);
        return gross.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    public boolean contains(IndividualTicket ticket) {
        return tickets.contains(ticket);
    }

    public void removeTicket(IndividualTicket ticket) {
        tickets.remove(ticket);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
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
