package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.DataDirectories;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.ParkRepositories;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;
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

    private static final ParkController INSTANCE = new ParkController();

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;
    private PassRepository passes;
    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        initialize(ParkRepositories.inMemory(), LocalDate.now());
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares a new execution with in-memory groups, tickets and attractions.
     */
    public void initialize(UserRepository users, LocalDate today) {
        initialize(users, new GroupRepository(), today);
    }

    /**
     * Prepares a new execution with in-memory tickets and attractions.
     */
    public void initialize(UserRepository users, GroupRepository groups, LocalDate today) {
        initialize(users, groups, new TicketRepository(), new AttractionRepository(), new PassRepository(), today);
    }

    /**
     * Prepares a new execution with the given repositories.
     */
    public void initialize(ParkRepositories repositories, LocalDate today) {
        initialize(repositories.users(), repositories.groups(), repositories.tickets(),
                repositories.attractions(), repositories.passes(), today);
    }

    /**
     * Prepares a new execution: injects the repositories created during
     * application initialization into this controller and into the
     * controllers that use them, and wires their collaboration with this
     * controller.
     */
    public void initialize(UserRepository users, GroupRepository groups, TicketRepository tickets,
            AttractionRepository attractions, PassRepository passes, LocalDate today) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.passes = passes;
        this.currentDate = today;
        this.currentPrice = INITIAL_PRICE;
        UserService.getInstance().configure(users, groups, this, new BCryptPasswordHasher());
        GroupService.getInstance().configure(users, groups, this);
        TicketService.getInstance().configure(users, groups, tickets, passes, this);
        AttractionService.getInstance().configure(attractions, tickets, this);
        InvoiceService.getInstance().configure(users, groups, tickets, passes, this);
        PassService.getInstance().configure(passes, tickets, this);
    }

    /**
     * Loads the park state stored in the data directory and makes it the state
     * of a new execution starting on {@code today}.
     *
     * @return the number of malformed stored lines that were ignored
     * @throws PersistenceException if the stored data cannot be read
     */
    public int load(Path dataDir, LocalDate today) {
        ParkRepositories repositories = ParkRepositories.fromDirectory(dataDir);
        initialize(repositories, today);
        return repositories.ignoredLines();
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

    public PassRepository getPasses() {
        return passes;
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
