package es.upm.etsisi.park.model.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.model.group.Group;
import es.upm.etsisi.park.user.User;

/**
 * Ticket bought by a group, composed of one individual ticket per member at
 * purchase time. The composition never changes; the group reference is
 * cleared when the group is deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final List<IndividualTicket> members;

    public GroupTicket(String id, LocalDate date, Group group, List<IndividualTicket> members) {
        super(id, date);
        this.group = group;
        this.members = List.copyOf(members);
    }

    /** Derived identifier of the member ticket for the user with that DNI. */
    public static String memberId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    public Group getGroup() {
        return group;
    }

    /** Called when the group is deleted; historical tickets keep their composition. */
    public void detachGroup() {
        group = null;
    }

    public List<IndividualTicket> getMembers() {
        return members;
    }

    public int getDiscountPercent() {
        return members.isEmpty() ? 0 : members.get(0).getDiscountPercent();
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (IndividualTicket member : members) {
            total = total.add(member.getPrice());
        }
        return Money.of(total);
    }

    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket member : members) {
            holders.add(member.getUser());
        }
        return holders;
    }
}
