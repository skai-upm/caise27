package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A joint ticket bought by a group: one individual ticket per member at purchase
 * time, each priced at the base price minus the group discount. The total is the
 * sum of the composed tickets. The ticket refers to its group while the group
 * exists; the group does not refer to its tickets.
 */
public class GroupTicket extends Ticket {

    private final List<IndividualTicket> composition;
    private Group group;

    private GroupTicket(String id, Group group, LocalDate date, List<IndividualTicket> composition) {
        super(id, date, null);
        this.group = group;
        this.composition = Collections.unmodifiableList(composition);
    }

    /**
     * Builds a group ticket for the given members (email to DNI, in member order).
     * Composed ticket identifiers are {@code <id>:<dni>}.
     */
    public static GroupTicket purchase(String id, Group group, LocalDate date, BigDecimal basePrice,
                                       Map<String, String> memberDnis) {
        BigDecimal unitPrice = discounted(basePrice, discountPercent(memberDnis.size()));
        List<IndividualTicket> tickets = new ArrayList<>();
        memberDnis.forEach((email, dni) ->
                tickets.add(new IndividualTicket(composedId(id, dni), email, date, unitPrice, true)));
        return new GroupTicket(id, group, date, tickets);
    }

    public static String composedId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    /** Group-specific discount brackets by number of members included. */
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

    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<String> admittedUsers() {
        return composition.stream().map(IndividualTicket::getOwnerEmail).toList();
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    /** The purchasing group, or null once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    /** Called when the purchasing group is deleted: historical records keep everything else. */
    public void detachGroup() {
        group = null;
    }
}
