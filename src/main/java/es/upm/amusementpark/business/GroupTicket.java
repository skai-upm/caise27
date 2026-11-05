package es.upm.amusementpark.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Joint ticket of a group, composed of one individual ticket per member at purchase time. The composition never
 * changes; the reference to the purchasing group becomes {@code null} if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private final String groupId;
    private Group group;
    private final List<IndividualTicket> components;

    GroupTicket(String id, Group group, LocalDate date, List<IndividualTicket> components) {
        this(id, Objects.requireNonNull(group, "group").getId(), group, date, components);
    }

    /** Rebuilds a group ticket whose purchasing group may no longer exist ({@code group} may be null). */
    GroupTicket(String id, String groupId, Group group, LocalDate date, List<IndividualTicket> components) {
        super(id, date);
        this.group = group;
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        this.components = List.copyOf(components);
        for (IndividualTicket component : this.components) {
            component.setContainingGroupTicket(this);
        }
    }

    /**
     * Discount rate applied to each member's base price, by number of members included in the purchase.
     *
     * @param members number of members
     * @return 0, 0.10, 0.20 or 0.50
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

    /** @return the discounted price of each member's ticket */
    public static BigDecimal memberPrice(BigDecimal basePrice, int members) {
        return basePrice.multiply(BigDecimal.ONE.subtract(discountFor(members))).setScale(2, RoundingMode.HALF_UP);
    }

    /** @return the purchasing group, or {@code null} once it has been deleted */
    public Group getGroup() {
        return group;
    }

    /** @return identifier of the purchasing group, kept for historical records */
    public String getGroupId() {
        return groupId;
    }

    void clearGroup() {
        group = null;
    }

    public List<IndividualTicket> getComponents() {
        return components;
    }

    /** Total price: sum of the component prices. */
    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket component : components) {
            total = total.add(component.getPrice());
        }
        return total;
    }

    @Override
    public List<String> holders() {
        return components.stream().map(IndividualTicket::getUserEmail).toList();
    }

    /** Users are associated through the component tickets, which are registered on their own. */
    @Override
    public boolean concernsUser(String email) {
        return false;
    }
}
