package es.upm.park.service;

import es.upm.park.model.Group;
import es.upm.park.model.GroupDiscount;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.validation.UserValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation and deletion of individual and group tickets. All ticket types
 * share one identifier space, checked through {@link Park#isTicketIdTaken}.
 */
public class TicketService {

    private static final String DERIVED_ID_SEPARATOR = ":";

    private final Park park;

    public TicketService(Park park) {
        this.park = park;
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) throws OperationException {
        requireFreeId(id);
        User user = park.users().findByEmail(email);
        if (user == null) {
            throw new OperationException("No user with email " + email);
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.getCurrentPrice());
        park.addTicket(ticket);
        park.save();
        return ticket;
    }

    public void deleteTicket(String id) throws OperationException {
        Ticket ticket = park.findTicket(id);
        if (ticket == null) {
            throw new OperationException("No ticket with identifier " + id);
        }
        if (ticket instanceof GroupTicket) {
            throw new OperationException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        GroupTicket container = park.findContainingGroupTicket((IndividualTicket) ticket);
        if (container != null) {
            throw new OperationException("Ticket " + id + " belongs to group ticket " + container.getId()
                    + " and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        park.removeTicket(ticket);
        park.save();
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws OperationException {
        requireFreeId(id);
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new OperationException("No group with identifier " + groupId);
        }
        if (group.size() == 0) {
            throw new OperationException("Group " + groupId + " has no members");
        }
        requireNotPast(date);

        List<User> members = group.getMembers();
        GroupDiscount discount = GroupDiscount.forMembers(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String componentId = id + DERIVED_ID_SEPARATOR + member.getDni();
            requireFreeId(componentId);
            composition.add(new IndividualTicket(componentId, date, member, park.getCurrentPrice(), discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.addTicket(ticket);
        park.save();
        return ticket;
    }

    public void deleteGroupTicket(String id) throws OperationException {
        Ticket ticket = park.findTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationException("No group ticket with identifier " + id);
        }
        requireDeletable(ticket);
        park.removeTicket(ticket);
        park.save();
    }

    private void requireFreeId(String id) throws OperationException {
        if (!UserValidator.isNotBlank(id)) {
            throw new OperationException("The ticket identifier must not be empty");
        }
        if (park.isTicketIdTaken(id)) {
            throw new OperationException("A ticket with identifier " + id + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) throws OperationException {
        if (date.isBefore(park.getCurrentDate())) {
            throw new OperationException("Tickets cannot be created for past dates (" + date + ")");
        }
    }

    private void requireDeletable(Ticket ticket) throws OperationException {
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            throw new OperationException("Ticket " + ticket.getId()
                    + " is for today or an earlier date and is kept as a historical record");
        }
    }
}
