package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group: it aggregates the group and is composed of one individual ticket
 * per member at the time of purchase, with a discount that depends on the number of members.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Group group;
    private final List<IndividualTicket> tickets;
    private final int discountPercent;

    /**
     * Builds a group ticket for the given holders, creating its internal individual tickets.
     */
    public GroupTicket(String id, LocalDate date, Group group, List<User> holders, List<BigDecimal> prices,
                       int discountPercent) {
        super(id, date);
        this.group = group;
        this.discountPercent = discountPercent;
        List<IndividualTicket> created = new ArrayList<>();
        for (int i = 0; i < holders.size(); i++) {
            created.add(new IndividualTicket(id + "-" + (i + 1), date, holders.get(i), prices.get(i)));
        }
        this.tickets = Collections.unmodifiableList(created);
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

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal sum = BigDecimal.ZERO;
        for (IndividualTicket ticket : tickets) {
            sum = sum.add(ticket.getPrice());
        }
        return sum.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getVisitors() {
        List<User> visitors = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            visitors.add(ticket.getHolder());
        }
        return visitors;
    }
}
