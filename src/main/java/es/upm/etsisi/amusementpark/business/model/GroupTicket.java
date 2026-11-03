package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A ticket bought by a group for a date. It aggregates its {@link Group} and is composed of one individual
 * {@link Ticket} per member at purchase time; later membership changes do not alter the composition.
 * The total is the sum of the individual prices with a discount depending on the number of members.
 */
public class GroupTicket extends ParkTicket {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private final Group group;
    private final String groupId;
    private final BigDecimal unitPrice;
    private final BigDecimal price;
    private final List<Ticket> tickets;

    /** Buys a group ticket: creates one constituent ticket for each current member of the group. */
    public GroupTicket(String id, Group group, LocalDate date, BigDecimal unitPrice) {
        this(id, group, group.getId(), date, unitPrice, constituents(id, group.getMembers(), date, unitPrice), null);
    }

    /** Restores a stored group ticket; {@code group} is null when the group no longer exists. */
    public GroupTicket(String id, Group group, String groupId, LocalDate date, BigDecimal unitPrice,
                       List<Ticket> tickets, BigDecimal storedPrice) {
        super(id, date);
        this.group = group;
        this.groupId = groupId;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.UNNECESSARY);
        this.tickets = new ArrayList<>(tickets);
        this.price = storedPrice != null ? storedPrice.setScale(2, RoundingMode.HALF_UP)
                : total(this.unitPrice, this.tickets.size());
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

    /** Price of {@code members} individual tickets with the size discount applied. */
    public static BigDecimal total(BigDecimal unitPrice, int members) {
        BigDecimal sum = unitPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountFor(members)));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Price stored in each constituent ticket: the individual price minus the group discount. */
    public static BigDecimal memberShare(BigDecimal unitPrice, int members) {
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountFor(members)));
        return unitPrice.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Builds the constituent ticket identifiers {@code <id>-1 .. <id>-n}. */
    public static String constituentId(String groupTicketId, int position) {
        return groupTicketId + "-" + position;
    }

    private static List<Ticket> constituents(String id, List<User> members, LocalDate date, BigDecimal unitPrice) {
        List<Ticket> result = new ArrayList<>(members.size());
        BigDecimal share = memberShare(unitPrice, members.size());
        for (int i = 0; i < members.size(); i++) {
            result.add(new Ticket(constituentId(id, i + 1), members.get(i), date, share));
        }
        return result;
    }

    /** The group that bought the ticket; empty for a historical ticket whose group no longer exists. */
    public Optional<Group> getGroup() {
        return Optional.ofNullable(group);
    }

    public String getGroupId() {
        return groupId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** The constituent individual tickets, one per member at purchase time. */
    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    @Override
    public List<User> getVisitors() {
        return tickets.stream().flatMap(t -> t.getVisitors().stream()).toList();
    }

    public boolean belongsTo(Group candidate) {
        return group == candidate;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }
}
