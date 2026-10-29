package es.upm.amusementpark.business.model.ticket;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase time.
 * The composition never changes; the group reference becomes null if the group is deleted.
 */
public class GroupTicket extends Ticket implements GroupAccess {

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

    public Group getGroup() {
        return group;
    }

    /**
     * Drops the reference to the purchasing group when that group is deleted.
     */
    public void detachGroup() {
        this.group = null;
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    public boolean contains(IndividualTicket ticket) {
        return composition.contains(ticket);
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : composition) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            users.add(ticket.getUser());
        }
        return users;
    }

    @Override
    public List<IndividualTicket> getAccessTickets() {
        return composition;
    }
}
