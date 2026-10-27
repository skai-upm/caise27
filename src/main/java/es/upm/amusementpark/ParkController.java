package es.upm.amusementpark;

import java.math.BigDecimal;
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
