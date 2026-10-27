package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Central park management. Exactly one instance exists per execution; it coordinates
 * the park state (users, and later groups, tickets and attractions), the current system
 * date and the current base ticket price.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static ParkController instance;

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        start(new UserRepository());
    }

    public static ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /** Initialises the state of a new execution: today's date and the initial price. */
    public void start(UserRepository users) {
        this.users = users;
        this.groups = new GroupRepository();
        this.tickets = new TicketRepository();
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    /** Whether any ticket dated today or later involves the user (derived, never stored). */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket.involves(user) && ticket.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** A group is locked while it has an associated group ticket dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group && gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the user belongs to any locked group. */
    public boolean isInLockedGroup(User user) {
        for (Group group : groups.findAll()) {
            if (group.contains(user) && isGroupLocked(group)) {
                return true;
            }
        }
        return false;
    }

    /** Deletes a group, clearing the reference held by its (historical) group tickets. */
    public void deleteGroup(Group group) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                gt.clearGroup();
            }
        }
        groups.remove(group);
    }

    /** Removes a user from every group it belongs to. */
    public void removeFromAllGroups(User user) {
        for (Group group : groups.findAll()) {
            group.removeMember(user);
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}
