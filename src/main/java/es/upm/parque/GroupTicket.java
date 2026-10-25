package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one {@link IndividualTicket} per member
 * at the time of purchase.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final String groupId;
    private final List<IndividualTicket> tickets = new ArrayList<>();

    /**
     * @param group   group that bought the ticket, or null for history of a deleted group
     * @param groupId identifier of that group, kept for history
     */
    public GroupTicket(String id, LocalDate date, BigDecimal price, Group group, String groupId) {
        super(id, date, price);
        this.group = group;
        this.groupId = groupId;
    }

    public Group getGroup() {
        return group;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    void addTicket(IndividualTicket ticket) {
        tickets.add(ticket);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            users.addAll(t.getUsers());
        }
        return users;
    }

    /**
     * Discount rate applied to the sum of individual prices, by number of members.
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

    /**
     * Total price for a group ticket: sum of individual prices minus the member discount.
     */
    public static BigDecimal totalFor(BigDecimal unitPrice, int members) {
        BigDecimal sum = unitPrice.multiply(BigDecimal.valueOf(members));
        return sum.subtract(sum.multiply(discountFor(members))).setScale(2, RoundingMode.HALF_UP);
    }
}
