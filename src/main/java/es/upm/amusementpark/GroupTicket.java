package es.upm.amusementpark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ticket bought by a group: composed of one individual ticket per member at purchase time.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final List<IndividualTicket> tickets;
    private final BigDecimal price;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> tickets, BigDecimal price) {
        super(id, date);
        this.group = group;
        this.tickets = new ArrayList<>(tickets);
        this.price = price;
    }

    /** Builds a group ticket for the current members of {@code group}, applying the size discount. */
    public static GroupTicket purchase(String id, LocalDate date, Group group, BigDecimal basePrice) {
        List<User> members = group.getMembers();
        BigDecimal factor = BigDecimal.ONE.subtract(discountFor(members.size()));
        BigDecimal unitPrice = basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(members.size())).multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);
        List<IndividualTicket> tickets = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            tickets.add(new IndividualTicket(constituentId(id, i + 1), date, members.get(i), unitPrice));
        }
        return new GroupTicket(id, date, group, tickets, total);
    }

    static String constituentId(String groupTicketId, int position) {
        return groupTicketId + "-" + position;
    }

    /** Discount rate for a group of the given size: &lt;3 0%, 3-5 10%, 6-10 20%, &gt;10 50%. */
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

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            users.add(t.getUser());
        }
        return users;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }
}
