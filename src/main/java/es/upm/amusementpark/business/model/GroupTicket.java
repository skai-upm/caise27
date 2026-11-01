package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one individual ticket per
 * group member at purchase time; later membership changes do not affect it.
 */
public class GroupTicket extends Ticket {

    private final Group group;
    private final List<IndividualTicket> tickets;

    /** Buys a ticket for the current members of the group at the given base price. */
    public GroupTicket(String id, Group group, LocalDate date, BigDecimal basePrice) {
        this(id, group, date, totalPrice(basePrice, group.getMembers().size()),
                createTickets(id, group.getMembers(), date, basePrice));
    }

    /** Rebuilds a stored group ticket from its already existing parts. */
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

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            users.add(ticket.getUser());
        }
        return users;
    }

    /** Identifier given to the constituent ticket at the given 1-based position. */
    public static String constituentId(String groupTicketId, int position) {
        return groupTicketId + "-" + position;
    }

    /** Discount applied to a group of the given size: 0%, 10%, 20% or 50%. */
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

    /** Discounted price paid for each member's ticket. */
    public static BigDecimal memberPrice(BigDecimal basePrice, int members) {
        return basePrice.multiply(BigDecimal.ONE.subtract(discountFor(members))).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal totalPrice(BigDecimal basePrice, int members) {
        return memberPrice(basePrice, members).multiply(BigDecimal.valueOf(members));
    }

    private static List<IndividualTicket> createTickets(String id, List<User> members, LocalDate date,
                                                        BigDecimal basePrice) {
        BigDecimal price = memberPrice(basePrice, members.size());
        List<IndividualTicket> created = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            created.add(new IndividualTicket(constituentId(id, i + 1), date, price, members.get(i)));
        }
        return created;
    }
}
