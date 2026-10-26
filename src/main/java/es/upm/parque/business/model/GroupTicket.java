package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group: composed of one individual ticket per member at purchase time.
 * Its price is the discounted total.
 */
public class GroupTicket extends Ticket implements GroupAdmission {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, Group group, BigDecimal total, List<IndividualTicket> tickets) {
        super(id, date, total);
        this.group = group;
        this.tickets = new ArrayList<>(tickets);
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public boolean contains(IndividualTicket ticket) {
        return tickets.contains(ticket);
    }

    public void addTicket(IndividualTicket ticket) {
        tickets.add(ticket);
    }

    public boolean removeTicket(IndividualTicket ticket) {
        return tickets.remove(ticket);
    }

    @Override
    public List<User> getUsers() {
        return tickets.stream().map(IndividualTicket::getUser).toList();
    }

    /**
     * Discount percentage applied for a group of the given size.
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

    /**
     * Total price for the given number of members at the given individual price.
     */
    public static BigDecimal totalFor(BigDecimal unitPrice, int members) {
        BigDecimal sum = unitPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountFor(members)));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
