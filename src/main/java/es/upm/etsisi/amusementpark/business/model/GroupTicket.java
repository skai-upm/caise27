package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A joint ticket bought by a group, composed of one individual ticket per member
 * at purchase time. The composition never changes afterwards.
 */
public class GroupTicket extends Ticket implements GroupAdmission {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private Group group;
    private final List<IndividualTicket> composition;

    private GroupTicket(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(identifier, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /**
     * Builds a group ticket for the current members of {@code group}, applying the
     * member-count discount to {@code basePrice} for every individual ticket.
     */
    public static GroupTicket purchase(String identifier, LocalDate date, Group group, BigDecimal basePrice) {
        List<User> members = group.getMembers();
        BigDecimal unitPrice = basePrice
                .multiply(HUNDRED.subtract(BigDecimal.valueOf(discountFor(members.size()))))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        List<IndividualTicket> parts = new ArrayList<>();
        for (User member : members) {
            parts.add(new IndividualTicket(identifier + ":" + member.getDni(), date, member, unitPrice));
        }
        return new GroupTicket(identifier, date, group, parts);
    }

    /**
     * Rebuilds a previously purchased group ticket with its stored composition.
     * {@code group} is {@code null} when the purchasing group no longer exists.
     */
    public static GroupTicket restore(String identifier, LocalDate date, Group group,
            List<IndividualTicket> composition) {
        return new GroupTicket(identifier, date, group, composition);
    }

    /**
     * Discount percentage for the number of members included in the purchase.
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

    /** The purchasing group, or {@code null} once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    public void detachGroup() {
        group = null;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return composition;
    }
}
