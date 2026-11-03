package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.ParkTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.GroupTicketRepository;
import es.upm.etsisi.amusementpark.persistence.TicketRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Individual and group ticket operations. Every date check uses the system date supplied by the controller.
 * Persists the tickets after every successful change. Rule violations are reported with
 * {@link IllegalArgumentException}.
 */
public class TicketController {

    private final TicketRepository storage;
    private final GroupTicketRepository groupStorage;
    private final UserController users;
    private final List<Ticket> tickets;
    private final List<GroupTicket> groupTickets;

    private static volatile TicketController instance;

    /**
     * Creates the unique instance of this controller for the current execution with its injected
     * dependencies. Called once by the composition root at startup; calling it again starts a new execution.
     */
    public static synchronized TicketController initialize(TicketRepository storage, GroupTicketRepository groupStorage,
                                                       UserController users, GroupController groups) {
        instance = new TicketController(storage, groupStorage, users, groups);
        return instance;
    }

    /** The unique instance of the current execution. */
    public static TicketController getInstance() {
        TicketController current = instance;
        if (current == null) {
            throw new IllegalStateException("TicketController has not been initialized");
        }
        return current;
    }

    private TicketController(TicketRepository storage, GroupTicketRepository groupStorage,
                             UserController users, GroupController groups) {
        this.storage = storage;
        this.groupStorage = groupStorage;
        this.users = users;
        this.tickets = new ArrayList<>(storage.load(users.getUsers()));
        this.groupTickets = new ArrayList<>(groupStorage.load(users.getUsers(), groups.getGroups()));
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    public Optional<GroupTicket> findGroupTicket(String id) {
        return groupTickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /** An individual or group ticket that can be used to enter attractions (not a constituent ticket). */
    public Optional<ParkTicket> findUsable(String id) {
        Optional<ParkTicket> individual = find(id).map(ParkTicket.class::cast);
        return individual.isPresent() ? individual : findGroupTicket(id).map(ParkTicket.class::cast);
    }

    /** Every ticket of the park through the common abstraction: individual, group and constituent tickets. */
    public List<ParkTicket> getAllTickets() {
        List<ParkTicket> all = new ArrayList<>(tickets);
        for (GroupTicket g : groupTickets) {
            all.add(g);
            all.addAll(g.getTickets());
        }
        return all;
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Optional<Ticket> find(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /** Buys a ticket for a registered user, for a date on or after {@code today}, at {@code price}. */
    public Ticket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        require(UserValidator.isNotBlank(id), "Ticket identifier must not be empty");
        requireUnusedId(id);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registered user with email " + email));
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        Ticket ticket = new Ticket(id, user, date, price);
        tickets.add(ticket);
        saveOrRevert(() -> tickets.remove(ticket));
        return ticket;
    }

    /** Deletes a ticket whose date is strictly later than {@code today}. */
    public void delete(String id, LocalDate today) {
        Ticket ticket = find(id).orElseThrow(() -> new IllegalArgumentException("No ticket with identifier " + id));
        require(ticket.getDate().isAfter(today),
                "Ticket " + id + " cannot be deleted: its date " + ticket.getDate() + " is not after " + today);
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        saveOrRevert(() -> tickets.add(index, ticket));
    }

    /** Buys a group ticket: one constituent ticket per current member, discounted by group size. */
    public GroupTicket createGroupTicket(String id, Group group, LocalDate date, LocalDate today, BigDecimal price) {
        require(UserValidator.isNotBlank(id), "Ticket identifier must not be empty");
        requireUnusedId(id);
        require(!group.getMembers().isEmpty(), "Group " + group.getId() + " has no members");
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        GroupTicket ticket = new GroupTicket(id, group, date, price);
        groupTickets.add(ticket);
        saveGroupsOrRevert(() -> groupTickets.remove(ticket));
        return ticket;
    }

    /** Deletes a group ticket, with all its constituent tickets, if dated strictly after {@code today}. */
    public void deleteGroupTicket(String id, LocalDate today) {
        GroupTicket ticket = findGroupTicket(id)
                .orElseThrow(() -> new IllegalArgumentException("No group ticket with identifier " + id));
        require(ticket.getDate().isAfter(today),
                "Group ticket " + id + " cannot be deleted: its date " + ticket.getDate() + " is not after " + today);
        int index = groupTickets.indexOf(ticket);
        groupTickets.remove(index);
        saveGroupsOrRevert(() -> groupTickets.add(index, ticket));
    }

    /** True if the user owns an individual or constituent ticket dated on or after {@code today}. */
    public boolean hasActiveTickets(User user, LocalDate today) {
        return getAllTickets().stream().anyMatch(t -> t instanceof Ticket ticket
                && ticket.belongsTo(user) && ticket.isValidOn(today));
    }

    /** True while the group has a group ticket dated on or after {@code today}. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        return groupTickets.stream().anyMatch(t -> t.belongsTo(group) && t.isValidOn(today));
    }

    private void requireUnusedId(String id) {
        require(getAllTickets().stream().noneMatch(t -> t.getId().equals(id)),
                "A ticket with identifier " + id + " already exists");
    }

    private void saveOrRevert(Runnable revert) {
        try {
            storage.save(tickets);
        } catch (RuntimeException e) {
            revert.run();
            throw e;
        }
    }

    private void saveGroupsOrRevert(Runnable revert) {
        try {
            groupStorage.save(groupTickets);
        } catch (RuntimeException e) {
            revert.run();
            throw e;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
