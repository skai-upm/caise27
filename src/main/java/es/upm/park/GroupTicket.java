package es.upm.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group: composed of one individual ticket per member at purchase time.
 * Its price is the sum of the individual tickets minus a discount by number of members.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Group group;
    private final BigDecimal basePrice;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, Group group, BigDecimal basePrice, List<IndividualTicket> tickets) {
        super(id, date, totalPrice(basePrice, tickets.size()));
        this.group = group;
        this.basePrice = basePrice;
        this.tickets = Collections.unmodifiableList(new ArrayList<>(tickets));
    }

    /** Creates the group ticket with one individual ticket per current member of the group. */
    public static GroupTicket purchase(String id, LocalDate date, Group group, BigDecimal basePrice) {
        List<IndividualTicket> constituents = new ArrayList<>();
        for (User member : group.getMembers()) {
            constituents.add(new IndividualTicket(constituentId(id, member), date, basePrice, member));
        }
        return new GroupTicket(id, date, group, basePrice, constituents);
    }

    /** Identifier of the individual ticket of {@code member} inside group ticket {@code id}. */
    public static String constituentId(String id, User member) {
        return id + "-" + member.getDni();
    }

    /** Discount percentage for a group ticket with this number of members. */
    public static int discountPercent(int members) {
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

    /** Sum of {@code members} tickets at {@code basePrice} minus the member-count discount. */
    public static BigDecimal totalPrice(BigDecimal basePrice, int members) {
        BigDecimal sum = basePrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountPercent(members)));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public Group getGroup() {
        return group;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    @Override
    public List<User> getHolders() {
        return tickets.stream().map(IndividualTicket::getUser).toList();
    }
}
