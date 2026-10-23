package es.upm.park.business.model;

import es.upm.park.business.model.access.GroupAccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket bought by a group, composed of one individual ticket per member
 * at purchase time. The composition never changes after creation; the
 * group reference becomes {@code null} once the group is deleted.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(id, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    public Group getGroup() {
        return group;
    }

    /**
     * Drops the reference to a group that no longer exists.
     */
    public void detachGroup() {
        this.group = null;
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
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
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            holders.add(ticket.getUser());
        }
        return holders;
    }

    @Override
    public List<IndividualTicket> getAdmittedTickets() {
        return composition;
    }
}
