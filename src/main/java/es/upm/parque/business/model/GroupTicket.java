package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group for one date. It is composed of one {@link IndividualTicket} per member of the
 * group at purchase time and refers to the group it was bought for. Its price is the sum of the individual
 * prices with a discount that depends on the number of members.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String groupId;
    private Group group;
    private final BigDecimal basePrice;
    private final int discountPercent;
    private final List<IndividualTicket> components;

    /** Buys a ticket for the current members of {@code group}, generating one internal ticket per member. */
    public GroupTicket(String id, LocalDate date, Group group, BigDecimal basePrice) {
        super(id, date);
        this.groupId = group.getId();
        this.group = group;
        this.basePrice = basePrice;
        List<IndividualTicket> generated = new ArrayList<>();
        for (User member : group.getMembers()) {
            generated.add(new IndividualTicket(componentId(id, generated.size() + 1), date, basePrice,
                member.getDni(), member, id));
        }
        this.components = generated;
        this.discountPercent = discountFor(generated.size());
    }

    /** Restores a stored group ticket; {@code group} is null if that group no longer exists. */
    public GroupTicket(String id, LocalDate date, String groupId, Group group, BigDecimal basePrice,
            int discountPercent, List<IndividualTicket> components) {
        super(id, date);
        this.groupId = groupId;
        this.group = group;
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
        this.components = new ArrayList<>(components);
    }

    /** Discount percentage for a group of {@code members} people. */
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

    /** Identifier of the internal ticket at {@code position} (1-based) of a group ticket. */
    public static String componentId(String groupTicketId, int position) {
        return groupTicketId + "-" + position;
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal sum = BigDecimal.ZERO;
        for (IndividualTicket t : components) {
            sum = sum.add(t.getPrice());
        }
        return sum.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
            .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getAdmittedUsers() {
        List<User> admitted = new ArrayList<>();
        for (IndividualTicket t : components) {
            admitted.addAll(t.getAdmittedUsers());
        }
        return admitted;
    }

    public String getGroupId() {
        return groupId;
    }

    /** @return the group the ticket was bought for, or null if that group no longer exists */
    public Group getGroup() {
        return group;
    }

    public boolean isForGroup(Group candidate) {
        return group == candidate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public List<IndividualTicket> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /** Called when the group is deleted; the ticket stays as a historical record. */
    public void detachGroup() {
        group = null;
    }
}
