package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * A joint ticket composed of one individual ticket per group member at purchase
 * time. The composition never changes; the group reference becomes null when the
 * group is deleted.
 */
public class GroupTicket extends Ticket implements GroupAccess {
    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(identifier, date);
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    /**
     * Discount for a group ticket covering this many members.
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

    public static String compositionIdentifier(String groupTicketIdentifier, User member) {
        return groupTicketIdentifier + ":" + member.getDni();
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    @Override
    public List<User> getUsers() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }

    @Override
    public List<IndividualTicket> getEntryTickets() {
        return composition;
    }
}
