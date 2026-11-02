package es.upm.etsisi.amusementpark.ticket;

import es.upm.etsisi.amusementpark.group.Group;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase time.
 * The composition never changes; the group reference is cleared if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /** Discount percentage for a purchase covering the given number of members. */
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

    /** Identifier of the constituent ticket for a member. */
    public static String constituentId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    /** The purchasing group, or {@code null} once that group has been deleted. */
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return composition;
    }
}
