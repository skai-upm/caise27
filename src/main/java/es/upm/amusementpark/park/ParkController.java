package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.AttractionFileStore;
import es.upm.amusementpark.persistence.DataPaths;
import es.upm.amusementpark.persistence.GroupFileStore;
import es.upm.amusementpark.persistence.TicketFileStore;
import es.upm.amusementpark.persistence.UserFileStore;
import es.upm.amusementpark.repository.AttractionRepository;
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
    private AttractionRepository attractions;
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

    /** Starts an execution with in-memory groups, tickets and attractions (no persistence). */
    public void start(UserRepository users) {
        start(users, new GroupRepository(), new TicketRepository(), new AttractionRepository());
    }

    /** Starts an execution whose repositories persist in the files of the given data directory. */
    public void start(DataPaths paths) {
        UserRepository loadedUsers = new UserRepository(new UserFileStore(paths.users()));
        GroupRepository loadedGroups = new GroupRepository(new GroupFileStore(paths.groups()), loadedUsers);
        TicketRepository loadedTickets = new TicketRepository(new TicketFileStore(paths.tickets()), loadedUsers,
                loadedGroups);
        AttractionRepository loadedAttractions = new AttractionRepository(new AttractionFileStore(paths.attractions()));
        start(loadedUsers, loadedGroups, loadedTickets, loadedAttractions);
    }

    /** Initialises the state of a new execution: repositories, today's date and the initial price. */
    private void start(UserRepository users, GroupRepository groups, TicketRepository tickets,
            AttractionRepository attractions) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public AttractionRepository getAttractions() {
        return attractions;
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
        tickets.save();
        groups.remove(group);
    }

    /** Removes a user from every attraction it is inside. */
    public void removeFromAllAttractions(User user) {
        for (Attraction attraction : attractions.findAll()) {
            attraction.exit(user);
        }
    }

    /** Removes a user from every group it belongs to. */
    public void removeFromAllGroups(User user) {
        for (Group group : groups.findAll()) {
            group.removeMember(user);
        }
        groups.save();
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
