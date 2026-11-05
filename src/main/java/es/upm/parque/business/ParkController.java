package es.upm.parque.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

/**
 * Central park management: the single object that coordinates the overall park state
 * during an execution (users, current system date and current base ticket price).
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private ParkRepositories repositories;
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

    /**
     * Starts a new execution with the repositories configured during initialization (see
     * {@link ParkConfiguration}) and resets the date to today and the price to 40.00.
     */
    public void startExecution(ParkRepositories repositories) {
        this.repositories = Objects.requireNonNull(repositories);
        this.currentDate = LocalDate.now(ZoneId.systemDefault());
        this.basePrice = INITIAL_PRICE;
    }

    /** The injected repositories; fails clearly when no execution has been started. */
    private ParkRepositories repositories() {
        if (repositories == null) {
            throw new IllegalStateException(ParkConfiguration.NOT_INITIALIZED);
        }
        return repositories;
    }

    /** Saves groups, tickets and attractions when the execution is backed by files. */
    public void persist() {
        repositories().save();
    }

    public UserRepository getUsers() {
        return repositories().getUsers();
    }

    public GroupRepository getGroups() {
        return repositories().getGroups();
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
        for (Ticket ticket : getTickets().findAll()) {
            if (ticket instanceof GroupTicket groupTicket) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    public AttractionRepository getAttractions() {
        return repositories().getAttractions();
    }

    public TicketRepository getTickets() {
        return repositories().getTickets();
    }

    public PassRepository getPasses() {
        return repositories().getPasses();
    }

    /** Whether the user has an active ticket, derived by traversing the tickets managed by the system. */
    public boolean hasActiveTicket(User user) {
        return getTickets().individualTicketsOf(user).stream().anyMatch(t -> t.isActiveOn(currentDate));
    }

    /** Whether the user belongs to a group that currently has an active group ticket. */
    public boolean belongsToLockedGroup(User user) {
        return getGroups().findAll().stream().anyMatch(g -> g.contains(user) && hasActiveGroupTicket(g));
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
