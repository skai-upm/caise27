package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.persistence.AttractionFileStorage;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.DataDirectories;
import es.upm.amusementpark.persistence.GroupFileStorage;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TicketFileStorage;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserFileStorage;
import es.upm.amusementpark.persistence.UserRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central management of the park state during one execution: the users,
 * groups, tickets, attractions, the current system date and the current base
 * ticket price. Only one instance exists.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    static final String USERS_FILE = "users.txt";
    static final String GROUPS_FILE = "groups.txt";
    static final String TICKETS_FILE = "tickets.txt";
    static final String ATTRACTIONS_FILE = "attractions.txt";

    private static final ParkController INSTANCE = new ParkController();

    private UserRepository users = new UserRepository();
    private GroupRepository groups = new GroupRepository();
    private TicketRepository tickets = new TicketRepository();
    private AttractionRepository attractions = new AttractionRepository();
    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares the park state for a new execution.
     */
    public void initialize(UserRepository users, LocalDate today) {
        initialize(users, new GroupRepository(), today);
    }

    /**
     * Prepares the park state for a new execution.
     */
    public void initialize(UserRepository users, GroupRepository groups, LocalDate today) {
        initialize(users, groups, new TicketRepository(), new AttractionRepository(), today);
    }

    /**
     * Prepares the park state for a new execution.
     */
    public void initialize(UserRepository users, GroupRepository groups, TicketRepository tickets,
            AttractionRepository attractions, LocalDate today) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.currentDate = today;
        this.currentPrice = INITIAL_PRICE;
    }

    /**
     * Loads the park state stored in the data directory and makes it the state
     * of a new execution starting on {@code today}.
     *
     * @return the number of malformed stored lines that were ignored
     * @throws PersistenceException if the stored data cannot be read
     */
    public int load(Path dataDir, LocalDate today) {
        UserFileStorage userStorage = new UserFileStorage(dataDir.resolve(USERS_FILE));
        UserRepository loadedUsers = new UserRepository(userStorage);
        GroupFileStorage groupStorage = new GroupFileStorage(dataDir.resolve(GROUPS_FILE));
        GroupRepository loadedGroups = new GroupRepository(groupStorage, loadedUsers);
        TicketFileStorage ticketStorage = new TicketFileStorage(dataDir.resolve(TICKETS_FILE));
        TicketRepository loadedTickets = new TicketRepository(ticketStorage, loadedUsers, loadedGroups);
        AttractionFileStorage attractionStorage = new AttractionFileStorage(dataDir.resolve(ATTRACTIONS_FILE));
        AttractionRepository loadedAttractions = new AttractionRepository(attractionStorage);
        initialize(loadedUsers, loadedGroups, loadedTickets, loadedAttractions, today);
        return userStorage.getSkippedLines() + groupStorage.getSkippedLines() + ticketStorage.getSkippedLines()
                + attractionStorage.getSkippedLines();
    }

    /**
     * Creates a new temporary data directory inside {@code baseDir} for an
     * isolated execution.
     *
     * @throws PersistenceException if the directory cannot be created
     */
    public Path createTemporaryDataDirectory(Path baseDir) {
        return DataDirectories.createTemporary(baseDir);
    }

    /**
     * Deletes a temporary data directory and its files.
     *
     * @throws PersistenceException if it cannot be deleted
     */
    public void deleteTemporaryDataDirectory(Path dir, Path baseDir) {
        DataDirectories.deleteTemporary(dir, baseDir);
    }

    /** Writes the whole park state to the repositories' storages. */
    public void persistAll() {
        users.persist();
        groups.persist();
        tickets.persist();
        attractions.persist();
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

    public AttractionRepository getAttractions() {
        return attractions;
    }

    /**
     * Whether the user has an active ticket (dated on or after the current
     * system date), found by traversing the park's tickets.
     */
    public boolean hasActiveTicket(User user) {
        for (IndividualTicket ticket : tickets.findIndividualTicketsOf(user)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the group has an associated group ticket dated on or after the
     * current system date, found by traversing the park's tickets.
     */
    public boolean hasActiveGroupTicket(Group group) {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Group tickets that reference the group, found by traversing the tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    /** Whether any group the user belongs to has an active group ticket. */
    public boolean belongsToLockedGroup(User user) {
        for (Group group : groups.findAll()) {
            if (group.contains(user) && hasActiveGroupTicket(group)) {
                return true;
            }
        }
        return false;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Replaces the current base ticket price.
     *
     * @throws IllegalArgumentException if the price is not positive
     */
    public void setCurrentPrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("The price must be positive.");
        }
        this.currentPrice = price.setScale(2);
    }
}
