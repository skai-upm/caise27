package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Main controller of the application. It owns the single system date and the current
 * ticket price, and enforces the rules that involve more than one kind of entity.
 */
public class ParkController {

    static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private LocalDate systemDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    /**
     * Creates the controller with its data files in the given directory. The system date starts
     * as the operating system's date; this is the only place the real clock is read.
     */
    public ParkController(Path dataDir) {
        this(dataDir, LocalDate.now(ZoneId.systemDefault()));
    }

    ParkController(Path dataDir, LocalDate initialDate) {
        this.systemDate = initialDate;
        this.users = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        this.groups = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), users::findByDni);
        this.tickets = new TicketManager(
                new TicketRepository(dataDir.resolve("tickets.txt"), dataDir.resolve("grouptickets.txt")),
                users::findByDni, groups::find);
        this.attractions = new AttractionManager(new AttractionRepository(dataDir.resolve("attractions.txt")));
    }

    public UserManager getUsers() {
        return users;
    }

    public GroupManager getGroups() {
        return groups;
    }

    public TicketManager getTickets() {
        return tickets;
    }

    public AttractionManager getAttractions() {
        return attractions;
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public LocalDate setSystemDate(String text) {
        systemDate = parseDate(text);
        return systemDate;
    }

    public BigDecimal setTicketPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + text);
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return ticketPrice;
    }

    // ---- users ----

    public void createUser(String name, String surname, String dni, String email,
                           String password, String phone, String card) {
        users.create(name, surname, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String newValue) {
        users.update(email, password, field, newValue);
    }

    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (tickets.hasCurrentOrFutureTicket(user, systemDate)) {
            throw new IllegalArgumentException("User " + email + " has tickets dated on or after " + systemDate);
        }
        groups.removeUserEverywhere(user);
        users.remove(user);
    }

    // ---- groups ----

    public void createGroup(String id, String name) {
        groups.create(id, name);
    }

    public void addToGroup(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        groups.addMember(group, existingUser(email));
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        groups.removeMember(group, existingUser(email));
    }

    public void deleteGroup(String groupId) {
        groups.delete(unlockedGroup(groupId));
    }

    // ---- tickets ----

    public IndividualTicket createTicket(String id, String email, String date) {
        return tickets.createIndividual(id, existingUser(email), parseDate(date), ticketPrice, systemDate);
    }

    public void deleteTicket(String id) {
        tickets.deleteIndividual(id, systemDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) {
        return tickets.createGroupTicket(id, groups.get(groupId), parseDate(date), ticketPrice, systemDate);
    }

    public void deleteGroupTicket(String id) {
        tickets.deleteGroupTicket(id, systemDate);
    }

    // ---- attractions ----

    public void createAttraction(String id, String name, String capacity) {
        attractions.create(id, name, capacity);
    }

    public void deleteAttraction(String id) {
        attractions.delete(id);
    }

    public int enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.get(attractionId);
        return attraction.enter(tickets.get(ticketId), systemDate);
    }

    public int exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.get(attractionId);
        return attraction.exit(tickets.get(ticketId));
    }

    public Attraction getAttraction(String id) {
        return attractions.get(id);
    }

    private User existingUser(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return user;
    }

    private Group unlockedGroup(String groupId) {
        Group group = groups.get(groupId);
        if (tickets.isGroupLocked(group, systemDate)) {
            throw new IllegalArgumentException("Group " + groupId + " is locked by a group ticket dated on or after "
                    + systemDate);
        }
        return group;
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }
}
