package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group. It is composed of one individual ticket per member at the time of
 * purchase; its price is the sum of those tickets minus a discount based on the number of members.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Group group;
    private final List<IndividualTicket> tickets;
    private final int discountPercent;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> tickets) {
        super(id, date);
        this.group = group;
        this.tickets = new ArrayList<>(tickets);
        this.discountPercent = discountFor(tickets.size());
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
        return Collections.unmodifiableList(tickets);
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public boolean contains(IndividualTicket ticket) {
        return tickets.contains(ticket);
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal sum = tickets.stream().map(IndividualTicket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
