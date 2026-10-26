package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one individual ticket per member at purchase time and its price is
 * the sum of those tickets minus a discount that depends on the number of members.
 */
public class GroupTicket extends Ticket implements GroupAccess {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final Group group;
    private final List<IndividualTicket> tickets;
    private final BigDecimal totalPrice;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> tickets, BigDecimal totalPrice) {
        super(id, date);
        this.group = group;
        this.tickets = List.copyOf(tickets);
        this.totalPrice = totalPrice;
    }

    /** Builds a group ticket whose total is computed from its individual tickets and the size discount. */
    public static GroupTicket purchase(String id, LocalDate date, Group group, List<IndividualTicket> tickets) {
        return new GroupTicket(id, date, group, tickets, totalFor(tickets));
    }

    /** Discount percentage for a group of the given size. */
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

    static BigDecimal totalFor(List<IndividualTicket> tickets) {
        BigDecimal sum = BigDecimal.ZERO;
        for (IndividualTicket t : tickets) {
            sum = sum.add(t.getPrice());
        }
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountFor(tickets.size())));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return tickets;
    }

    @Override
    public BigDecimal getPrice() {
        return totalPrice;
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
