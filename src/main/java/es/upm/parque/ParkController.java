package es.upm.parque;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the park. It owns the single system date and the ticket price, and coordinates the rules
 * that involve several kinds of objects (users, groups, tickets and attractions). Every operation returns a
 * message starting with "OK" or "ERROR".
 */
public class ParkController {

    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int DATE_LENGTH = 10;

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    /**
     * Loads all stored data from {@code paths}; the system date starts as the operating-system date.
     */
    public ParkController(StoragePaths paths) throws IOException {
        this(paths, LocalDate.now());
    }

    public ParkController(StoragePaths paths, LocalDate startDate) throws IOException {
        this.currentDate = startDate;
        this.users = new UserManager(new UserRepository(paths.users()));
        this.groups = new GroupManager(new GroupRepository(paths.groups()), users::findByDni);
        this.tickets = new TicketManager(new TicketRepository(paths.tickets(), paths.groupTickets()),
                users::findByDni, groups::find);
        this.attractions = new AttractionManager(new AttractionRepository(paths.attractions()));
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public UserManager getUserManager() {
        return users;
    }

    public GroupManager getGroupManager() {
        return groups;
    }

    public TicketManager getTicketManager() {
        return tickets;
    }

    public AttractionManager getAttractionManager() {
        return attractions;
    }

    /**
     * Parses a date in the exact format yyyy-MM-dd, returning {@code null} if it is not a valid date.
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != DATE_LENGTH) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a positive amount with at most two decimals, returning {@code null} otherwise.
     */
    public static BigDecimal parsePrice(String text) {
        try {
            BigDecimal price = new BigDecimal(text);
            if (price.signum() <= 0 || price.scale() > 2) {
                return null;
            }
            return price.setScale(2);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // ---- System -------------------------------------------------------------------------------------------

    public String setSystemDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return "ERROR Invalid date: " + text + " (expected yyyy-MM-dd)";
        }
        currentDate = date;
        return "OK SYSTEM DATE " + currentDate;
    }

    public String setTicketPrice(String text) {
        BigDecimal price = parsePrice(text);
        if (price == null) {
            return "ERROR Invalid price: " + text + " (expected a positive amount with up to 2 decimals)";
        }
        ticketPrice = price;
        return "OK SYSTEM PRICE " + ticketPrice.toPlainString();
    }

    // ---- Users --------------------------------------------------------------------------------------------

    public String createUser(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        return users.createUser(name, surname, dni, email, password, phone, card);
    }

    public String updateUser(String email, String password, UserField field, String value) {
        return users.updateUser(email, password, field, value);
    }

    /**
     * Deletes a user that has no ticket dated on or after the system date, together with its past tickets and
     * its group memberships.
     */
    public String deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (user == null) {
            return "ERROR Unknown email or wrong password";
        }
        if (tickets.hasCurrentOrFutureTicket(user, currentDate)) {
            return "ERROR " + user.getEmail() + " has tickets dated on or after " + currentDate
                    + " and cannot be deleted";
        }
        if (!tickets.removeTicketsOf(user) || !groups.removeUserEverywhere(user) || !users.remove(user)) {
            return "ERROR Could not save changes";
        }
        return "OK USER DELETED " + user.getEmail();
    }

    // ---- Groups -------------------------------------------------------------------------------------------

    public String createGroup(String id, String name) {
        return groups.create(id, name);
    }

    public String addToGroup(String groupId, String email) {
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        if (tickets.isGroupLocked(group, currentDate)) {
            return lockedError(group);
        }
        return groups.addMember(group, user);
    }

    public String removeFromGroup(String groupId, String email) {
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        if (tickets.isGroupLocked(group, currentDate)) {
            return lockedError(group);
        }
        return groups.removeMember(group, user);
    }

    /**
     * Deletes an unlocked group together with its past group tickets.
     */
    public String deleteGroup(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        if (tickets.isGroupLocked(group, currentDate)) {
            return lockedError(group);
        }
        if (!tickets.removeGroupTicketsOf(group) || !groups.delete(group)) {
            return "ERROR Could not save changes";
        }
        return "OK GROUP DELETED " + groupId;
    }

    private String lockedError(Group group) {
        return "ERROR Group " + group.getId() + " has group tickets dated on or after " + currentDate
                + " and cannot be modified";
    }

    // ---- Tickets ------------------------------------------------------------------------------------------

    public String createTicket(String id, String email, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        IndividualTicket ticket = new IndividualTicket(id, parseDate(dateText), user, ticketPrice);
        if (!tickets.add(ticket)) {
            return "ERROR Could not save tickets";
        }
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    public String deleteTicket(String id) {
        Ticket found = tickets.find(id);
        if (!(found instanceof IndividualTicket ticket)) {
            return "ERROR Unknown ticket: " + id;
        }
        GroupTicket owner = tickets.groupTicketOf(ticket);
        if (owner != null) {
            return "ERROR Ticket " + id + " belongs to group ticket " + owner.getId()
                    + "; delete the group ticket instead";
        }
        if (!ticket.isFuture(currentDate)) {
            return "ERROR Only tickets dated after " + currentDate + " can be deleted";
        }
        return tickets.delete(ticket) ? "OK TICKET DELETED " + id : "ERROR Could not save tickets";
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        String error = checkNewTicket(id, dateText);
        if (error != null) {
            return error;
        }
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        if (group.getMembers().isEmpty()) {
            return "ERROR Group " + groupId + " has no members";
        }
        for (User member : group.getMembers()) {
            String internalId = GroupTicket.internalTicketId(id, member);
            if (tickets.isIdTaken(internalId)) {
                return "ERROR Ticket " + internalId + " already exists";
            }
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, parseDate(dateText), group, ticketPrice);
        if (!tickets.add(groupTicket)) {
            return "ERROR Could not save tickets";
        }
        return "OK GROUP TICKET CREATED -> " + groupTicket.getPrice().toPlainString();
    }

    public String deleteGroupTicket(String id) {
        if (!(tickets.find(id) instanceof GroupTicket groupTicket)) {
            return "ERROR Unknown group ticket: " + id;
        }
        if (!groupTicket.isFuture(currentDate)) {
            return "ERROR Only group tickets dated after " + currentDate + " can be deleted";
        }
        return tickets.delete(groupTicket) ? "OK GROUP TICKET DELETED " + id : "ERROR Could not save tickets";
    }

    private String checkNewTicket(String id, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            return "ERROR Ticket identifier must not be empty";
        }
        if (tickets.isIdTaken(id)) {
            return "ERROR Ticket " + id + " already exists";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR Invalid date: " + dateText + " (expected yyyy-MM-dd)";
        }
        if (date.isBefore(currentDate)) {
            return "ERROR Tickets cannot be bought for a date before " + currentDate;
        }
        return null;
    }

    // ---- Attractions --------------------------------------------------------------------------------------

    public String createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public String deleteAttraction(String id) {
        return attractions.delete(id);
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return "ERROR Unknown ticket: " + ticketId;
        }
        if (!ticket.isValidOn(currentDate)) {
            return "ERROR Ticket " + ticketId + " is for " + ticket.getDate() + ", not for " + currentDate;
        }
        try {
            int entered = attraction.enter(ticket);
            return "OK ENTERED " + attractionId + " -> " + entered + " user(s)";
        } catch (IllegalStateException e) {
            return "ERROR " + e.getMessage();
        }
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return "ERROR Unknown ticket: " + ticketId;
        }
        try {
            int left = attraction.exit(ticket);
            return "OK EXITED " + attractionId + " -> " + left + " user(s)";
        } catch (IllegalStateException e) {
            return "ERROR " + e.getMessage();
        }
    }

    public String attractionStatus(String attractionId) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + attractionId;
        }
        List<String> present = new ArrayList<>();
        for (User user : attraction.getPresent()) {
            present.add(user.getName() + " " + user.getSurname() + " <" + user.getEmail() + ">");
        }
        return String.join(System.lineSeparator(),
                "OK ATTRACTION STATUS " + attraction.getId(),
                "  Name: " + attraction.getName(),
                "  Maximum capacity: " + attraction.getCapacity(),
                "  Users present: " + attraction.getPresent().size(),
                "  Available places: " + attraction.getAvailablePlaces(),
                "  Present: " + (present.isEmpty() ? "(none)" : String.join(", ", present)));
    }
}
