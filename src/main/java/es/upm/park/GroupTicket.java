package es.upm.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a {@link Group} for one date. It aggregates the group and is
 * composed of one {@link IndividualTicket} per member at purchase time; the total
 * is the sum of the individual prices minus a discount by member count.
 */
public class GroupTicket extends Ticket {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private final Group group;
    private final String groupId;
    private final BigDecimal individualPrice;
    private final BigDecimal total;
    private final List<IndividualTicket> constituents;

    private GroupTicket(String id, Group group, String groupId, LocalDate date, BigDecimal individualPrice,
                        BigDecimal total, List<IndividualTicket> constituents) {
        super(id, date);
        this.group = group;
        this.groupId = groupId;
        this.individualPrice = individualPrice;
        this.total = total;
        this.constituents = Collections.unmodifiableList(constituents);
    }

    /** Buys a group ticket for the current members of {@code group}. */
    public static GroupTicket purchase(String id, Group group, LocalDate date, BigDecimal individualPrice) {
        return of(id, group, group.getId(), date, individualPrice, null, group.getMembers(), null);
    }

    /**
     * Rebuilds a stored group ticket. {@code owners} and {@code ownerDnis} are parallel
     * lists; an owner may be {@code null} for a deleted user. A {@code null} total is
     * recomputed from the price and member count.
     */
    static GroupTicket of(String id, Group group, String groupId, LocalDate date, BigDecimal individualPrice,
                          BigDecimal total, List<User> owners, List<String> ownerDnis) {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (int k = 0; k < owners.size(); k++) {
            User owner = owners.get(k);
            String dni = ownerDnis != null ? ownerDnis.get(k) : owner.getDni();
            tickets.add(new IndividualTicket(constituentId(id, k + 1), owner, dni, date, individualPrice));
        }
        BigDecimal amount = total != null ? total : totalFor(individualPrice, tickets.size());
        return new GroupTicket(id, group, groupId, date, individualPrice, amount, tickets);
    }

    /** Identifier of the k-th (1-based) constituent ticket. */
    public static String constituentId(String groupTicketId, int k) {
        return groupTicketId + "-" + k;
    }

    /** Discount percentage: &lt;3 → 0, 3–5 → 10, 6–10 → 20, &gt;10 → 50. */
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

    public static BigDecimal totalFor(BigDecimal individualPrice, int members) {
        BigDecimal sum = individualPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountPercent(members)));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket c : constituents) {
            holders.addAll(c.getHolders());
        }
        return holders;
    }

    @Override
    public BigDecimal getPrice() {
        return total;
    }

    public BigDecimal getIndividualPrice() {
        return individualPrice;
    }

    /** The group that bought the ticket, or {@code null} if it was deleted later. */
    public Group getGroup() {
        return group;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<IndividualTicket> getConstituents() {
        return constituents;
    }

    public boolean belongsTo(Group g) {
        return group != null && group == g;
    }
}
