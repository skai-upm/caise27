package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A ticket bought by a group. It is composed of one {@link IndividualTicket}
 * per member at purchase time and aggregates the {@link Group} that bought it.
 * If the group is later deleted, its identifier is kept as history.
 */
public class GroupTicket extends Ticket {

    private static final int SMALL_GROUP_LIMIT = 3;
    private static final int MEDIUM_GROUP_LIMIT = 5;
    private static final int LARGE_GROUP_LIMIT = 10;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String groupId;
    private Group group;
    private final BigDecimal basePrice;
    private final BigDecimal totalPrice;
    private final List<IndividualTicket> parts;

    /** Buys a ticket for the current members of {@code group} at {@code basePrice} each. */
    public GroupTicket(String id, LocalDate date, Group group, BigDecimal basePrice) {
        super(id, date);
        this.groupId = group.getId();
        this.group = group;
        this.basePrice = basePrice;
        List<IndividualTicket> generated = new ArrayList<>();
        for (User member : group.getMembers()) {
            generated.add(new IndividualTicket(partId(id, member.getDni()), date, member, basePrice));
        }
        this.parts = generated;
        this.totalPrice = totalFor(basePrice, generated.size());
    }

    /** Restores a stored group ticket ({@code group} may be null if it no longer exists). */
    public GroupTicket(String id, LocalDate date, String groupId, Group group, BigDecimal basePrice,
                       BigDecimal totalPrice, List<IndividualTicket> parts) {
        super(id, date);
        this.groupId = groupId;
        this.group = group;
        this.basePrice = basePrice;
        this.totalPrice = totalPrice;
        this.parts = new ArrayList<>(parts);
    }

    /** Identifier of the individual ticket generated for a member. */
    public static String partId(String groupTicketId, String dni) {
        return groupTicketId + "-" + dni;
    }

    /** Discount percentage for a group of {@code members} people. */
    public static int discountPercent(int members) {
        if (members < SMALL_GROUP_LIMIT) {
            return 0;
        }
        if (members <= MEDIUM_GROUP_LIMIT) {
            return 10;
        }
        if (members <= LARGE_GROUP_LIMIT) {
            return 20;
        }
        return 50;
    }

    /** Sum of {@code members} individual prices minus the group discount, two decimals. */
    public static BigDecimal totalFor(BigDecimal basePrice, int members) {
        BigDecimal sum = basePrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountPercent(members)));
        return sum.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getPrice() {
        return totalPrice;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getGroupId() {
        return groupId;
    }

    /** The buying group, or empty if it has been deleted. */
    public Optional<Group> getGroup() {
        return Optional.ofNullable(group);
    }

    public boolean belongsTo(Group candidate) {
        return group != null && group == candidate;
    }

    public void detachGroup() {
        group = null;
    }

    /** The users of all constituent tickets. */
    @Override
    public List<User> getVisitors() {
        List<User> visitors = new ArrayList<>();
        for (IndividualTicket part : parts) {
            visitors.addAll(part.getVisitors());
        }
        return visitors;
    }

    /** The constituent individual tickets (fixed at purchase). */
    public List<IndividualTicket> getParts() {
        return Collections.unmodifiableList(parts);
    }
}
