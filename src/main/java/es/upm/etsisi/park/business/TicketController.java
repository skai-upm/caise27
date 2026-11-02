package es.upm.etsisi.park.business;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Individual and group ticket purchase and deletion; deletions cascade to passes through the
 * injected {@link PassController}.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();

    private Park park;
    private PassController passes;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park, PassController passController) {
        this.park = park;
        this.passes = passController;
    }

    public IndividualTicket create(String id, String email, String date) {
        Park.requireNotBlank(id);
        requireFreeId(id);
        User user = park.findUser(email);
        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        LocalDate day = requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, park.getBasePrice(), 0);
        Map<String, Ticket> tickets = park.tickets();
        tickets.put(id, ticket);
        park.commit(() -> tickets.remove(id));
        return ticket;
    }

    public void delete(String id) {
        Ticket ticket = park.tickets().get(id);
        if (ticket == null) {
            throw new NotFoundException(park.findTicket(id) != null
                    ? "Ticket belongs to a group ticket and cannot be deleted on its own: " + id
                    : "Ticket not found: " + id);
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new RuleViolationException("Not an individual ticket (use GROUPTICKET DELETE): " + id);
        }
        removeFuture(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) {
        Park.requireNotBlank(id);
        requireFreeId(id);
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found: " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new RuleViolationException("Group has no members: " + groupId);
        }
        LocalDate day = requireNotPast(date);
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String componentId = GroupTicket.componentId(id, member.getDni());
            requireFreeId(componentId);
            composition.add(new IndividualTicket(componentId, day, member, park.getBasePrice(), discount));
        }
        GroupTicket ticket = new GroupTicket(id, day, group, composition);
        Map<String, Ticket> tickets = park.tickets();
        tickets.put(id, ticket);
        park.commit(() -> tickets.remove(id));
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = park.tickets().get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new NotFoundException("Group ticket not found: " + id);
        }
        removeFuture(ticket);
    }

    /** Removes a future ticket together with every pass of its entry tickets, all or nothing. */
    private void removeFuture(Ticket ticket) {
        if (!ticket.isDeletableOn(park.getToday())) {
            throw new RuleViolationException("Only tickets dated after "
                    + ValueParser.formatDate(park.getToday()) + " can be deleted");
        }
        Map<String, Ticket> tickets = park.tickets();
        Map<String, Ticket> before = new LinkedHashMap<>(tickets);
        Runnable restorePasses = passes.detachPassesOf(ticket.getEntryTickets());
        tickets.remove(ticket.getId());
        park.commit(() -> {
            Park.restore(tickets, before);
            restorePasses.run();
        });
    }

    private void requireFreeId(String id) {
        if (park.findTicket(id) != null) {
            throw new ConflictException("Ticket identifier already in use: " + id);
        }
    }

    private LocalDate requireNotPast(String date) {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(park.getToday())) {
            throw new RuleViolationException("Date is in the past: " + date);
        }
        return day;
    }
}
