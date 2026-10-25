package es.upm.parque.business.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.TicketRepository;
import es.upm.parque.business.validation.Identifiers;

/**
 * Every ticket (individual, internal to a group ticket, and group tickets) in a single
 * identifier space. Time-dependent rules use the system date of {@link ParkController}. Singleton.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private TicketRepository repository;
    private ParkController park;
    private UserController users;
    private GroupController groups;
    private PassController passes;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(TicketRepository repository, ParkController park, UserController users,
                          GroupController groups, PassController passes) {
        this.repository = repository;
        this.park = park;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
    }

    public void load() {
        tickets.clear();
        for (Ticket t : repository.load(users::findByDni, groups::find)) {
            tickets.put(t.getId(), t);
        }
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets.values());
    }

    public List<IndividualTicket> getIndividualTickets() {
        return tickets.values().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .toList();
    }

    public Ticket get(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket not found: " + id);
        }
        return ticket;
    }

    /** Individual ticket lookup, or null when missing or not individual. */
    public IndividualTicket findIndividual(String id) {
        return tickets.get(id) instanceof IndividualTicket t ? t : null;
    }

    public IndividualTicket getIndividual(String id) {
        if (!(get(id) instanceof IndividualTicket ticket)) {
            throw new IllegalArgumentException(id + " is a group ticket, not an individual ticket");
        }
        return ticket;
    }

    /** Individual tickets owned by the user, bought directly or through group tickets. */
    public List<IndividualTicket> ticketsOf(User user) {
        return getIndividualTickets().stream().filter(t -> t.getUser() == user).toList();
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        return tickets.values().stream()
                .filter(t -> t instanceof GroupTicket g && g.getGroup() == group)
                .map(GroupTicket.class::cast)
                .toList();
    }

    public IndividualTicket createIndividual(String id, String email, String dateText) {
        User user = users.get(email);
        LocalDate date = ParkController.parseDate(dateText);
        requireNewTicket(id, date);
        IndividualTicket ticket = new IndividualTicket(id, date, park.getTicketPrice(), user, user.getDni(), null);
        tickets.put(id, ticket);
        save();
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        Group group = groups.get(groupId);
        LocalDate date = ParkController.parseDate(dateText);
        requireNewTicket(id, date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Group " + groupId + " has no members");
        }
        GroupTicket groupTicket = new GroupTicket(id, date,
                GroupTicket.totalFor(park.getTicketPrice(), members.size()), group, groupId);
        tickets.put(id, groupTicket);
        int n = 0;
        for (User member : members) {
            String internalId;
            do {
                n++;
                internalId = id + "-" + n;
            } while (tickets.containsKey(internalId));
            IndividualTicket internal = new IndividualTicket(internalId, date, park.getTicketPrice(), member,
                    member.getDni(), groupTicket);
            groupTicket.addTicket(internal);
            tickets.put(internalId, internal);
        }
        save();
        return groupTicket;
    }

    /**
     * Deletes a future individual ticket together with its passes. Nothing changes on failure.
     */
    public void deleteIndividual(String id) {
        if (!(get(id) instanceof IndividualTicket ticket)) {
            throw new IllegalArgumentException(id + " is a group ticket: use GROUPTICKET DELETE");
        }
        if (ticket.isPartOfGroupTicket()) {
            throw new IllegalArgumentException(id + " belongs to group ticket " + ticket.getGroupTicket().getId()
                    + ": use GROUPTICKET DELETE");
        }
        requireRefundable(ticket);
        ticket.removeAllPasses();
        tickets.remove(id);
        saveWithPasses();
    }

    /**
     * Deletes a future group ticket: first its members' passes, then its individual tickets,
     * then the group ticket itself. Nothing changes on failure.
     */
    public void deleteGroupTicket(String id) {
        if (!(get(id) instanceof GroupTicket groupTicket)) {
            throw new IllegalArgumentException(id + " is not a group ticket: use TICKET DELETE");
        }
        requireRefundable(groupTicket);
        for (IndividualTicket internal : groupTicket.getTickets()) {
            internal.removeAllPasses();
        }
        for (IndividualTicket internal : groupTicket.getTickets()) {
            tickets.remove(internal.getId());
        }
        tickets.remove(id);
        saveWithPasses();
    }

    /**
     * True if the user owns an individual ticket (bought directly or through a group) dated today or later.
     */
    public boolean hasCurrentOrFutureTicket(User user) {
        LocalDate today = park.getSystemDate();
        return ticketsOf(user).stream().anyMatch(t -> t.isCurrentOrFuture(today));
    }

    /**
     * True if the group has a group ticket dated today or later.
     */
    public boolean isGroupLocked(Group group) {
        LocalDate today = park.getSystemDate();
        return groupTicketsOf(group).stream().anyMatch(t -> t.isCurrentOrFuture(today));
    }

    private void requireNewTicket(String id, LocalDate date) {
        Identifiers.require(id, "ticket");
        if (tickets.containsKey(id)) {
            throw new IllegalArgumentException("Ticket already exists: " + id);
        }
        if (date.isBefore(park.getSystemDate())) {
            throw new IllegalArgumentException("Ticket date " + date + " is before the system date "
                    + park.getSystemDate());
        }
    }

    private void requireRefundable(Ticket ticket) {
        if (!ticket.isRefundable(park.getSystemDate())) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " dated " + ticket.getDate()
                    + " can only be deleted before that date (system date " + park.getSystemDate() + ")");
        }
    }

    private void save() {
        repository.save(tickets.values());
    }

    private void saveWithPasses() {
        save();
        passes.save();
    }
}
