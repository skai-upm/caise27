package es.upm.etsisi.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase time.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /**
     * Discount percentage for a purchase including the given number of members.
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

    public static String memberTicketId(String groupTicketId, User user) {
        return groupTicketId + ":" + user.getDni();
    }

    /**
     * Purchasing group, or null once that group has been deleted.
     */
    public Group getGroup() {
        return group;
    }

    public void detachGroup() {
        this.group = null;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return composition;
    }

    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<User> getHolders() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }
}
