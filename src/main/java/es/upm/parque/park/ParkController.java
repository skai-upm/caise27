package es.upm.parque.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.upm.parque.model.Group;
import es.upm.parque.model.GroupTicket;
import es.upm.parque.model.Ticket;
import es.upm.parque.model.User;
import es.upm.parque.persistence.ParkFileStore;
import es.upm.parque.repository.AttractionRepository;
import es.upm.parque.repository.GroupRepository;
import es.upm.parque.repository.TicketRepository;
import es.upm.parque.repository.UserRepository;

/**
 * Central park management: the single object that coordinates the overall park state
 * during an execution (users, current system date and current base ticket price).
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private UserRepository users = new UserRepository();
    private GroupRepository groups = new GroupRepository();
    private TicketRepository tickets = new TicketRepository();
    private AttractionRepository attractions = new AttractionRepository();
    private ParkFileStore store;
    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal basePrice = INITIAL_PRICE;

    private ParkController() {
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    /** Returns the only central park management instance. */
    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Starts a new execution: uses the given users and resets the date to today and the price to 40.00. */
    public void startExecution(UserRepository users) {
        this.users = users;
        this.groups = new GroupRepository();
        this.tickets = new TicketRepository();
        this.attractions = new AttractionRepository();
        this.store = null;
        this.currentDate = LocalDate.now(ZoneId.systemDefault());
        this.basePrice = INITIAL_PRICE;
    }

    /** Starts a new execution backed by files: groups, tickets and attractions are loaded from the store. */
    public void startExecution(UserRepository users, ParkFileStore store) {
        startExecution(users);
        store.load(users, groups, tickets, attractions);
        this.store = store;
    }

    /** Saves groups, tickets and attractions when the execution is backed by files. */
    public void persist() {
        if (store != null) {
            store.save(groups, tickets, attractions);
        }
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    /**
     * Whether the group has an active group ticket, i.e. an associated group ticket dated on or after
     * the current system date. Derived on every call, never stored.
     */
    public boolean hasActiveGroupTicket(Group group) {
        Objects.requireNonNull(group, "group");
        return groupTicketsOf(group).stream().anyMatch(t -> t.isActiveOn(currentDate));
    }

    /** Group tickets purchased by the group, found by traversing the tickets managed by the system. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        return groupTickets().stream().filter(t -> t.isFor(group)).toList();
    }

    private List<GroupTicket> groupTickets() {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket groupTicket) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    public AttractionRepository getAttractions() {
        return attractions;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    /** Whether the user has an active ticket, derived by traversing the tickets managed by the system. */
    public boolean hasActiveTicket(User user) {
        return tickets.individualTicketsOf(user).stream().anyMatch(t -> t.isActiveOn(currentDate));
    }

    /** Whether the user belongs to a group that currently has an active group ticket. */
    public boolean belongsToLockedGroup(User user) {
        return groups.findAll().stream().anyMatch(g -> g.contains(user) && hasActiveGroupTicket(g));
    }

    /** Whether the ticket is part of the composition of a group ticket, found by traversal. */
    public boolean isPartOfGroupTicket(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket");
        return groupTickets().stream().anyMatch(t -> t.includes(ticket));
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
