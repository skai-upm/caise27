package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupDiscount;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.PassRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TICKET CREATE / DELETE and GROUPTICKET CREATE / DELETE. Deleting a ticket also refunds
 * every pass attached to it. Single instance per execution.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();
    private static final String NAME = "TicketController";
    private static final String DERIVED_ID_SEPARATOR = ":";
    private static final String TICKET = "Ticket ";

    private TicketRepository tickets;
    private UserRepository users;
    private GroupRepository groups;
    private PassRepository passes;
    private SystemState state;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(TicketRepository tickets, UserRepository users, GroupRepository groups,
                          PassRepository passes, SystemState state) {
        this.tickets = tickets;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
        this.state = state;
    }

    public IndividualTicket createIndividual(String id, String email, String dateText) {
        requireFreeId(id);
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, state.getBasePrice());
        tickets.add(ticket);
        return ticket;
    }

    public void deleteIndividual(String id) {
        Ticket ticket = find(id);
        Checks.require(!(ticket instanceof GroupTicket),
                () -> new RuleViolationException(TICKET + id + " is a group ticket; use GROUPTICKET DELETE"));
        Checks.require(tickets.isTopLevel(ticket), () -> new RuleViolationException(
                TICKET + id + " belongs to a group ticket and cannot be deleted on its own"));
        requireDeletable(ticket);
        passes.removeAll(passesOf(ticket));
        tickets.remove(ticket);
    }

    public GroupTicket createGroup(String id, String groupId, String dateText) {
        requireFreeId(id);
        Group group = groups.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
        Checks.require(!group.isEmpty(), () -> new RuleViolationException("Group " + groupId + " has no members"));
        LocalDate date = requireUsableDate(dateText);

        List<User> members = group.getMembers();
        int discount = GroupDiscount.percentFor(members.size());
        BigDecimal basePrice = state.getBasePrice();
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String memberTicketId = id + DERIVED_ID_SEPARATOR + member.getDni();
            Checks.require(!tickets.isIdInUse(memberTicketId),
                    () -> new DuplicateException("Ticket identifier already in use: " + memberTicketId));
            composition.add(new IndividualTicket(memberTicketId, date, member, basePrice, discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        tickets.add(ticket);
        return ticket;
    }

    public void deleteGroup(String id) {
        Ticket ticket = find(id);
        Checks.require(ticket instanceof GroupTicket,
                () -> new RuleViolationException(TICKET + id + " is not a group ticket; use TICKET DELETE"));
        requireDeletable(ticket);
        passes.removeAll(passesOf(ticket));
        tickets.remove(ticket);
    }

    public Ticket find(String id) {
        return Checks.configured(tickets, NAME).findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    private List<Pass> passesOf(Ticket ticket) {
        List<Pass> attached = new ArrayList<>();
        for (IndividualTicket individual : ticket.getAdmittedTickets()) {
            attached.addAll(passes.findByTicket(individual));
        }
        return attached;
    }

    private void requireFreeId(String id) {
        Checks.configured(tickets, NAME);
        Checks.requireText(id, "Ticket identifier");
        Checks.require(!tickets.isIdInUse(id), () -> new DuplicateException("Ticket identifier already in use: " + id));
    }

    private LocalDate requireUsableDate(String dateText) {
        LocalDate date = Checks.parseDate(dateText);
        Checks.require(!date.isBefore(state.getCurrentDate()), () -> new RuleViolationException(
                "Tickets cannot be created for a past date (current date " + state.getCurrentDate() + ")"));
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        Checks.require(ticket.isDeletableOn(state.getCurrentDate()), () -> new RuleViolationException(
                "Only tickets dated after the current date (" + state.getCurrentDate() + ") can be deleted"));
    }
}
