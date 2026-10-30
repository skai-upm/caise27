package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation and deletion of individual and group tickets (Singleton). Deleting a ticket refunds its
 * passes through the injected {@link PassController}.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();

    private ParkController park;
    private PassController passes;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) {
        requireNewId(id);
        User user = park.users().findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        LocalDate date = requireNotPast(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.basePrice());
        park.tickets().add(ticket);
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = find(id);
        if (!(ticket instanceof IndividualTicket)) {
            throw new OperationNotAllowedException("Use GROUPTICKET DELETE for group ticket " + id);
        }
        if (!park.tickets().isTopLevel(ticket)) {
            throw new OperationNotAllowedException("Ticket " + id + " belongs to a group ticket");
        }
        refund(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        requireNewId(id);
        Group group = park.groups().findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
        LocalDate date = requireNotPast(dateText);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("Group " + groupId + " is empty");
        }
        int discount = GroupTicket.discountFor(members.size());
        BigDecimal price = park.basePrice();
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String memberId = GroupTicket.memberTicketId(id, member);
            requireNewId(memberId);
            composition.add(new IndividualTicket(memberId, date, member, price, discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.tickets().add(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = find(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new OperationNotAllowedException("Ticket " + id + " is not a group ticket");
        }
        refund(ticket);
    }

    public Ticket find(String id) {
        return park.tickets().findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    /**
     * Deletes a future ticket together with its composition and every pass bought for it.
     */
    private void refund(Ticket ticket) {
        if (!ticket.isDeletableOn(park.today())) {
            throw new OperationNotAllowedException("Only tickets dated after " + park.today() + " can be deleted");
        }
        passes.deleteAllFor(ticket.getIndividualTickets());
        park.tickets().remove(ticket);
    }

    private void requireNewId(String id) {
        if (id.isBlank()) {
            throw new ValidationException("Ticket identifier cannot be empty");
        }
        if (park.tickets().existsId(id)) {
            throw new DuplicateException("Ticket identifier already used: " + id);
        }
    }

    private LocalDate requireNotPast(String dateText) {
        LocalDate date = ParkController.parseDate(dateText);
        if (date.isBefore(park.today())) {
            throw new OperationNotAllowedException("Ticket date is in the past: " + date);
        }
        return date;
    }
}
