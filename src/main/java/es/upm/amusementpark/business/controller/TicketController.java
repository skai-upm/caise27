package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation and deletion of individual and group tickets (Singleton). Deleting a ticket also refunds its passes
 * through the injected pass controller.
 */
public final class TicketController {

    private static TicketController instance;

    private Park park;
    private TicketRepository tickets;
    private UserRepository users;
    private GroupRepository groups;
    private PassController passes;

    private TicketController() {
    }

    public static synchronized TicketController getInstance() {
        if (instance == null) {
            instance = new TicketController();
        }
        return instance;
    }

    public void configure(Park park, TicketRepository tickets, UserRepository users, GroupRepository groups,
                          PassController passes) {
        this.park = park;
        this.tickets = tickets;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
    }

    public IndividualTicket createIndividual(String id, String email, String dateText) {
        requireUnusedId(id);
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        LocalDate date = purchasableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.getPrice(), 0);
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Deletes a future individual ticket together with all its passes.
     */
    public void deleteIndividual(String id) {
        Ticket ticket = existing(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new InvalidDataException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (tickets.isComposed(ticket)) {
            throw new OperationNotAllowedException("ticket " + id
                    + " belongs to a group ticket and can only be deleted with its group ticket");
        }
        requireDeletable(ticket);
        passes.removePassesOf(List.of(individual));
        tickets.remove(ticket);
    }

    /**
     * Buys a ticket for every current member of the group. All identifiers are checked before anything is created.
     */
    public GroupTicket createGroup(String id, String groupId, String dateText) {
        requireUnusedId(id);
        Group group = groups.findById(groupId);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + groupId);
        }
        if (group.getMembers().isEmpty()) {
            throw new OperationNotAllowedException("group " + groupId + " has no members");
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
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Deletes a future group ticket: the passes of its composed tickets, the composed tickets and the ticket itself.
     */
    public void deleteGroup(String id) {
        Ticket ticket = existing(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new InvalidDataException("ticket " + id + " is not a group ticket");
        }
        requireDeletable(ticket);
        passes.removePassesOf(groupTicket.getComposition());
        tickets.remove(ticket);
    }

    public Ticket existing(String id) {
        Ticket ticket = tickets.findById(id);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + id);
        }
        return ticket;
    }

    private void requireUnusedId(String id) {
        if (tickets.exists(id)) {
            throw new DuplicateException("a ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate purchasableDate(String dateText) {
        LocalDate date = ParkFormat.parseDate(dateText);
        if (date.isBefore(park.getDate())) {
            throw new OperationNotAllowedException("tickets cannot be created for past dates (current date is "
                    + ParkFormat.formatDate(park.getDate()) + ")");
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletableOn(park.getDate())) {
            throw new OperationNotAllowedException("ticket " + ticket.getId()
                    + " is for the current day or earlier and is kept as a historical record");
        }
    }
}
