package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Joint ticket bought by a group, composed of one individual ticket per member at purchase time.
 * Its price is the sum of the prices of its composition. It knows its group (while the group
 * exists); members are reached through the composition, never through a parallel user list.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final List<IndividualTicket> composition;
    private Group group;

    public GroupTicket(String id, Group group, LocalDate date, List<IndividualTicket> composition) {
        super(id, date, total(composition));
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /** Identifier of the composition ticket of the member with the given DNI. */
    public static String componentId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    /** Discount percentage for the number of members included: &lt;3 0%, 3-5 10%, 6-10 20%, &gt;10 50%. */
    public static int discountPercentFor(int members) {
        if (members < 3) {
            return 0;
        }
        if (members <= 5) {
            return 10;
        }
        return members <= 10 ? 20 : 50;
    }

    /** Price of each composition ticket: the base price minus the member-count discount. */
    public static BigDecimal discountedPrice(BigDecimal basePrice, int members) {
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountPercentFor(members)));
        return basePrice.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal total(List<IndividualTicket> composition) {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    @Override
    public List<User> getHolders() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }

    /** The purchasing group, or {@code null} once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    public boolean isFor(Group other) {
        return group != null && group.getId().equals(other.getId());
    }

    public boolean includes(Ticket ticket) {
        return composition.stream().anyMatch(t -> t.getId().equals(ticket.getId()));
    }

    /** Clears the reference to the purchasing group when that group is deleted. */
    public void detachGroup() {
        this.group = null;
    }
}
