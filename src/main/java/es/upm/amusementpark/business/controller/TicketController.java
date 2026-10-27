package es.upm.amusementpark.business.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Business rules for buying and deleting individual and group tickets.
 */
public class TicketController {

    private final ParkController park;

    public TicketController(ParkController park) {
        this.park = park;
    }

    public IndividualTicket createIndividual(String id, String email, LocalDate date) throws ParkException {
        requireFreeId(id);
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, park.getCurrentPrice(), user);
        park.getTickets().add(ticket);
        return ticket;
    }

    public void deleteIndividual(String id) throws ParkException {
        if (park.getTickets().findGroupTicketContaining(id) != null) {
            throw new OperationNotAllowedException("Ticket belongs to a group ticket; use GROUPTICKET DELETE");
        }
        Ticket ticket = park.getTickets().findById(id);
        if (!(ticket instanceof IndividualTicket)) {
            throw new NotFoundException("Ticket not found");
        }
        if (!ticket.isDeletable(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Only tickets dated after the current date can be deleted");
        }
        park.getTickets().remove(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws ParkException {
        requireFreeId(id);
        Group group = park.getGroups().findById(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found");
        }
        requireNotPast(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("Group has no members");
        }
        Set<String> derived = new HashSet<>();
        for (User member : members) {
            String memberTicketId = GroupTicket.derivedId(id, member);
            if (!derived.add(memberTicketId) || !park.getTickets().isIdAvailable(memberTicketId)) {
                throw new DuplicateException("Ticket identifier already exists: " + memberTicketId);
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, park.getCurrentPrice(), group, members);
        park.getTickets().add(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = park.getTickets().findById(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new NotFoundException("Group ticket not found");
        }
        if (!ticket.isDeletable(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Only group tickets dated after the current date can be deleted");
        }
        park.getTickets().remove(ticket);
    }

    private void requireFreeId(String id) throws DuplicateException {
        if (!park.getTickets().isIdAvailable(id)) {
            throw new DuplicateException("Ticket identifier already exists");
        }
    }

    private void requireNotPast(LocalDate date) throws OperationNotAllowedException {
        if (date.isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Tickets cannot be created for past dates");
        }
    }
}
