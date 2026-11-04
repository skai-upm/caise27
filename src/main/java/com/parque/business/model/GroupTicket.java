package com.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group for one date. It aggregates the group that bought it and is composed of one
 * {@link IndividualTicket} per member at the time of purchase; later membership changes do not alter it.
 */
public class GroupTicket extends Ticket implements GroupAccess {
    private final Group group;
    private final String groupId;
    private final BigDecimal unitPrice;
    private final BigDecimal total;
    private final List<IndividualTicket> constituents;

    /**
     * @param group the buying group, or null for the history of a group deleted in an earlier execution
     */
    public GroupTicket(String id, Group group, String groupId, LocalDate date, BigDecimal unitPrice, BigDecimal total,
            List<IndividualTicket> constituents) {
        super(id, date);
        this.group = group;
        this.groupId = groupId;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.total = total.setScale(2, RoundingMode.HALF_UP);
        this.constituents = List.copyOf(constituents);
    }

    /** Discount percentage for a group of {@code members} people: <3 → 0, 3–5 → 10, 6–10 → 20, >10 → 50. */
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

    /** Sum of {@code members} individual prices minus the member-count discount, with two decimals. */
    public static BigDecimal totalFor(BigDecimal unitPrice, int members) {
        BigDecimal sum = unitPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = BigDecimal.valueOf(100 - discountPercent(members));
        return sum.multiply(factor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Stored price of each constituent ticket: the individual price reduced by the group discount. */
    public static BigDecimal shareFor(BigDecimal unitPrice, int members) {
        BigDecimal factor = BigDecimal.valueOf(100 - discountPercent(members));
        return unitPrice.multiply(factor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** The group that bought the ticket (null only for history of a deleted group). */
    public Group getGroup() {
        return group;
    }

    public String getGroupId() {
        return groupId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public BigDecimal getPrice() {
        return total;
    }

    /** The owners of all constituent tickets. */
    @Override
    public List<User> getVisitors() {
        List<User> visitors = new ArrayList<>();
        for (IndividualTicket t : constituents) {
            visitors.addAll(t.getVisitors());
        }
        return visitors;
    }

    /** The individual tickets composing this group ticket (read-only). */
    @Override
    public List<IndividualTicket> getConstituents() {
        return Collections.unmodifiableList(constituents);
    }

    public boolean contains(IndividualTicket ticket) {
        for (IndividualTicket t : constituents) {
            if (t == ticket) {
                return true;
            }
        }
        return false;
    }
}
