package es.upm.amusementpark;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation and deletion of individual and group tickets.
 */
public class TicketService {

    private final Park park = Park.getInstance();

    public IndividualTicket createIndividual(String id, String email, String dateText) {
        requireUnusedId(id);
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        LocalDate date = purchasableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.getPrice(), 0);
        park.getTickets().add(ticket);
        return ticket;
    }

    public void deleteIndividual(String id) {
        Ticket ticket = existingTicket(id);
        if (!(ticket instanceof IndividualTicket)) {
            throw new IllegalArgumentException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (park.getTickets().isComposed(ticket)) {
            throw new IllegalArgumentException("ticket " + id
                    + " belongs to a group ticket and can only be deleted with its group ticket");
        }
        requireDeletable(ticket);
        park.getTickets().remove(ticket);
    }

    /**
     * Buys a ticket for every current member of the group. All identifiers are checked before anything is created.
     */
    public GroupTicket createGroup(String id, String groupId, String dateText) {
        requireUnusedId(id);
        Group group = park.getGroups().findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("no group with identifier " + groupId);
        }
        if (group.getMembers().isEmpty()) {
            throw new IllegalArgumentException("group " + groupId + " has no members");
        }
        LocalDate date = purchasableDate(dateText);
        for (User member : group.getMembers()) {
            requireUnusedId(GroupTicket.composedId(id, member));
        }
        int discount = GroupTicket.discountFor(group.getMembers().size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : group.getMembers()) {
            composition.add(new IndividualTicket(GroupTicket.composedId(id, member), date, member, park.getPrice(),
                    discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.getTickets().add(ticket);
        return ticket;
    }

    public void deleteGroup(String id) {
        Ticket ticket = existingTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new IllegalArgumentException("ticket " + id + " is not a group ticket");
        }
        requireDeletable(ticket);
        park.getTickets().remove(ticket);
    }

    private void requireUnusedId(String id) {
        if (park.getTickets().exists(id)) {
            throw new IllegalArgumentException("a ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate purchasableDate(String dateText) {
        LocalDate date = ParkFormat.parseDate(dateText);
        if (date.isBefore(park.getDate())) {
            throw new IllegalArgumentException("tickets cannot be created for past dates (current date is "
                    + ParkFormat.formatDate(park.getDate()) + ")");
        }
        return date;
    }

    private Ticket existingTicket(String id) {
        Ticket ticket = park.getTickets().findById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("no ticket with identifier " + id);
        }
        return ticket;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletableOn(park.getDate())) {
            throw new IllegalArgumentException("ticket " + ticket.getId()
                    + " is for the current day or earlier and is kept as a historical record");
        }
    }
}
