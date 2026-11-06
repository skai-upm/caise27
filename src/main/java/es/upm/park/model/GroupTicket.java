package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A ticket bought by a group: one individual ticket per member at purchase
 * time, with identifier {@code <groupTicketId>:<dni>}. The composition never
 * changes; the group reference is cleared if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private Group group;
    private final String groupId;
    private final List<IndividualTicket> composition;

    public GroupTicket(String id, LocalDate date, BigDecimal basePrice, Group group, List<User> members) {
        super(id, date, basePrice, GroupDiscount.forMembers(members.size()));
        this.group = group;
        this.groupId = group.getId();
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : members) {
            tickets.add(new IndividualTicket(constituentId(id, member.getDni()), date, basePrice, getDiscount(), member));
        }
        this.composition = List.copyOf(tickets);
    }

    /** Identifier of the constituent ticket of a member. */
    public static String constituentId(String groupTicketId, String dni) {
        return groupTicketId + ":" + dni;
    }

    /** The purchasing group, or {@code null} once it has been deleted. */
    public Group getGroup() {
        return group;
    }

    /** Identifier of the purchasing group, kept for historical records. */
    public String getGroupId() {
        return groupId;
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    public void detachGroup() {
        group = null;
    }

    public Optional<IndividualTicket> findConstituent(String constituentId) {
        return composition.stream().filter(t -> t.getId().equals(constituentId)).findFirst();
    }

    /** Total charged: the sum of the constituent ticket prices. */
    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    @Override
    public List<String> ids() {
        List<String> ids = new ArrayList<>();
        ids.add(getId());
        composition.forEach(t -> ids.add(t.getId()));
        return ids;
    }

    @Override
    public List<User> holders() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }

    @Override
    public boolean involves(User user) {
        return composition.stream().anyMatch(t -> t.involves(user));
    }
}
