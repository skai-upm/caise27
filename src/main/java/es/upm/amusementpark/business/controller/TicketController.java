package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.ticket.GroupTicket;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.model.ticket.Ticket;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.validation.ValueParser;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all tickets in one shared identifier space: individual tickets, group tickets and the
 * individual tickets composing them.
 */
public final class TicketController {

    private static final TicketController INSTANCE = new TicketController();

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private TicketRepository repository;
    private SystemController system;
    private UserController users;
    private GroupController groups;
    private PassController passes;

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    public void configure(TicketRepository repository) {
        this.repository = repository;
    }

    public void setCollaborators(SystemController system, UserController users, GroupController groups,
                                 PassController passes) {
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.passes = passes;
    }

    public void load() throws PersistenceException {
        tickets.clear();
        try {
            for (Ticket ticket : repository.load(users.usersByDni(), groups.groupsById())) {
                tickets.put(ticket.getId(), ticket);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not load tickets: " + e.getMessage(), e);
        }
    }

    public void save() throws PersistenceException {
        try {
            repository.saveAll(tickets.values(), users::isActive);
        } catch (IOException e) {
            throw new PersistenceException("could not save tickets: " + e.getMessage(), e);
        }
    }

    public IndividualTicket createTicket(String id, String email, String date) throws ParkException {
        requireFreeId(id);
        User user = users.requireUser(email);
        LocalDate day = requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, system.getCurrentPrice(), 0);
        tickets.put(id, ticket);
        return ticket;
    }

    /**
     * Deletes a future standalone ticket together with all its passes.
     */
    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationNotAllowedException("not an individual ticket, use GROUPTICKET DELETE: " + id);
        }
        if (containingGroupTicket(individual) != null) {
            throw new OperationNotAllowedException("ticket belongs to a group ticket: " + id);
        }
        requireDeletable(ticket);
        passes.removePassesOf(List.of(individual));
        tickets.remove(id);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) throws ParkException {
        requireFreeId(id);
        Group group = groups.requireGroup(groupId);
        LocalDate day = requireNotPast(date);
        if (group.isEmpty()) {
            throw new OperationNotAllowedException("group has no members: " + groupId);
        }
        List<User> members = group.getMembers();
        for (User member : members) {
            requireFreeId(id + ":" + member.getDni());
        }
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(id + ":" + member.getDni(), day, member,
                    system.getCurrentPrice(), discount));
        }
        GroupTicket groupTicket = new GroupTicket(id, day, group, composition);
        tickets.put(id, groupTicket);
        for (IndividualTicket member : composition) {
            tickets.put(member.getId(), member);
        }
        return groupTicket;
    }

    /**
     * Deletes a future group ticket: first the passes of its composition, then the composition tickets,
     * then the group ticket. All checks run before anything is removed.
     */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new OperationNotAllowedException("not a group ticket, use TICKET DELETE: " + id);
        }
        requireDeletable(ticket);
        passes.removePassesOf(groupTicket.getComposition());
        for (IndividualTicket member : groupTicket.getComposition()) {
            tickets.remove(member.getId());
        }
        tickets.remove(id);
    }

    public boolean isActive(Ticket ticket) {
        return ticket.isActive(system.getCurrentDate());
    }

    public Ticket requireTicket(String id) throws NotFoundException {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + id);
        }
        return ticket;
    }

    public IndividualTicket requireIndividualTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationNotAllowedException("not an individual ticket: " + id);
        }
        return individual;
    }

    public Ticket findTicket(String id) {
        return tickets.get(id);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    public Map<String, Ticket> ticketsById() {
        return new LinkedHashMap<>(tickets);
    }

    /**
     * Individual tickets (standalone or in a composition) of a user, found by traversing all tickets.
     */
    public List<IndividualTicket> ticketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }

    /**
     * Individual tickets bought directly for a user, i.e. not part of a group ticket composition.
     */
    public List<IndividualTicket> standaloneTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : ticketsOf(user)) {
            if (containingGroupTicket(ticket) == null) {
                result.add(ticket);
            }
        }
        return result;
    }

    /**
     * Group tickets bought by a group, found by traversing all tickets.
     */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    private GroupTicket containingGroupTicket(IndividualTicket ticket) {
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    private void requireFreeId(String id) throws ParkException {
        if (id.isEmpty()) {
            throw new ValidationException("ticket identifier must not be empty");
        }
        if (tickets.containsKey(id)) {
            throw new DuplicateException("ticket identifier already exists: " + id);
        }
    }

    private LocalDate requireNotPast(String date) throws ParkException {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(system.getCurrentDate())) {
            throw new OperationNotAllowedException("date is in the past: " + date);
        }
        return day;
    }

    private void requireDeletable(Ticket ticket) throws OperationNotAllowedException {
        if (!ticket.isDeletable(system.getCurrentDate())) {
            throw new OperationNotAllowedException("only tickets dated after today can be deleted: " + ticket.getId());
        }
    }
}
