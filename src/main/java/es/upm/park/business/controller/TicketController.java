package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupDiscount;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.validation.UserValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation and deletion of individual and group tickets. All ticket types
 * share one identifier space, checked through {@link Park#isTicketIdTaken}.
 * Deleting a ticket refunds its passes through the injected
 * {@link PassController}. Single instance.
 */
public final class TicketController {

    private static final String DERIVED_ID_SEPARATOR = ":";

    private Park park;
    private PassController passes;

    private TicketController() {
    }

    private static final class Holder {
        private static final TicketController INSTANCE = new TicketController();
    }

    public static TicketController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("TicketController has not been configured");
        }
        return park;
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) throws ParkException {
        requireFreeId(id);
        User user = park().findUserByEmail(email);
        if (user == null) {
            throw new NotFoundException("No user with email " + email);
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park().getCurrentPrice());
        park().addTicket(ticket);
        park().save();
        return ticket;
    }

    /**
     * Deletes a future standalone individual ticket together with all its
     * passes.
     */
    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = park().findTicket(id);
        if (ticket == null) {
            throw new NotFoundException("No ticket with identifier " + id);
        }
        if (ticket instanceof GroupTicket) {
            throw new BusinessRuleException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        IndividualTicket individual = (IndividualTicket) ticket;
        GroupTicket container = park().findContainingGroupTicket(individual);
        if (container != null) {
            throw new BusinessRuleException("Ticket " + id + " belongs to group ticket " + container.getId()
                    + " and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        passes.removeAllFor(List.of(individual));
        park().removeTicket(ticket);
        park().save();
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws ParkException {
        requireFreeId(id);
        Group group = park().findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("No group with identifier " + groupId);
        }
        if (group.size() == 0) {
            throw new BusinessRuleException("Group " + groupId + " has no members");
        }
        requireNotPast(date);

        List<User> members = group.getMembers();
        GroupDiscount discount = GroupDiscount.forMembers(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String componentId = id + DERIVED_ID_SEPARATOR + member.getDni();
            requireFreeId(componentId);
            composition.add(new IndividualTicket(componentId, date, member, park().getCurrentPrice(), discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park().addTicket(ticket);
        park().save();
        return ticket;
    }

    /**
     * Deletes a future group ticket: first the members' passes, then the
     * composing tickets with the group ticket itself.
     */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = park().findTicket(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new NotFoundException("No group ticket with identifier " + id);
        }
        requireDeletable(ticket);
        passes.removeAllFor(groupTicket.getComposition());
        park().removeTicket(ticket);
        park().save();
    }

    private void requireFreeId(String id) throws ParkException {
        if (!UserValidator.isNotBlank(id)) {
            throw new ValidationException("The ticket identifier must not be empty");
        }
        if (park().isTicketIdTaken(id)) {
            throw new ConflictException("A ticket with identifier " + id + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) throws BusinessRuleException {
        if (date.isBefore(park().getCurrentDate())) {
            throw new BusinessRuleException("Tickets cannot be created for past dates (" + date + ")");
        }
    }

    private void requireDeletable(Ticket ticket) throws BusinessRuleException {
        if (!ticket.isDeletableOn(park().getCurrentDate())) {
            throw new BusinessRuleException("Ticket " + ticket.getId()
                    + " is for today or an earlier date and is kept as a historical record");
        }
    }
}
