package es.upm.etsisi.park.service;

import es.upm.etsisi.park.core.SystemState;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupDiscount;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.repository.GroupRepository;
import es.upm.etsisi.park.repository.TicketRepository;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TICKET CREATE / DELETE and GROUPTICKET CREATE / DELETE.
 */
public class TicketService {

    private static final String DERIVED_ID_SEPARATOR = ":";

    private final TicketRepository tickets;
    private final UserRepository users;
    private final GroupRepository groups;
    private final SystemState state;

    public TicketService(TicketRepository tickets, UserRepository users, GroupRepository groups, SystemState state) {
        this.tickets = tickets;
        this.users = users;
        this.groups = groups;
        this.state = state;
    }

    public IndividualTicket createIndividual(String id, String email, String dateText) {
        requireFreeId(id);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("User not found: " + email));
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, state.getBasePrice());
        tickets.add(ticket);
        return ticket;
    }

    public void deleteIndividual(String id) {
        Ticket ticket = find(id);
        Checks.require(!(ticket instanceof GroupTicket), "Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        Checks.require(tickets.isTopLevel(ticket),
                "Ticket " + id + " belongs to a group ticket and cannot be deleted on its own");
        requireDeletable(ticket);
        tickets.remove(ticket);
    }

    public GroupTicket createGroup(String id, String groupId, String dateText) {
        requireFreeId(id);
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new UserOperationException("Group not found: " + groupId));
        Checks.require(!group.isEmpty(), "Group " + groupId + " has no members");
        LocalDate date = requireUsableDate(dateText);

        List<User> members = group.getMembers();
        int discount = GroupDiscount.percentFor(members.size());
        BigDecimal basePrice = state.getBasePrice();
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String memberTicketId = id + DERIVED_ID_SEPARATOR + member.getDni();
            Checks.require(!tickets.isIdInUse(memberTicketId), "Ticket identifier already in use: " + memberTicketId);
            composition.add(new IndividualTicket(memberTicketId, date, member, basePrice, discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        tickets.add(ticket);
        return ticket;
    }

    public void deleteGroup(String id) {
        Ticket ticket = find(id);
        Checks.require(ticket instanceof GroupTicket, "Ticket " + id + " is not a group ticket; use TICKET DELETE");
        requireDeletable(ticket);
        tickets.remove(ticket);
    }

    private Ticket find(String id) {
        return tickets.findById(id).orElseThrow(() -> new UserOperationException("Ticket not found: " + id));
    }

    private void requireFreeId(String id) {
        Checks.require(UserValidator.isNotBlank(id), "Ticket identifier must not be empty");
        Checks.require(!tickets.isIdInUse(id), "Ticket identifier already in use: " + id);
    }

    private LocalDate requireUsableDate(String dateText) {
        LocalDate date = SystemService.Dates.parse(dateText);
        Checks.require(!date.isBefore(state.getCurrentDate()),
                "Tickets cannot be created for a past date (current date " + state.getCurrentDate() + ")");
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        Checks.require(ticket.isDeletableOn(state.getCurrentDate()),
                "Only tickets dated after the current date (" + state.getCurrentDate() + ") can be deleted");
    }
}
