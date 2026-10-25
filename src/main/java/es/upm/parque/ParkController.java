package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Main controller of the park. It owns the single system date and the current ticket price and
 * applies the rules that involve several kinds of objects.
 */
public class ParkController {

    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TicketManager ticketManager;
    private final AttractionManager attractionManager;
    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    public ParkController(UserManager userManager, GroupManager groupManager,
                          TicketManager ticketManager, AttractionManager attractionManager) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.ticketManager = ticketManager;
        this.attractionManager = attractionManager;
    }

    /**
     * Creates a controller whose persistence files all live in {@code dataDir}.
     */
    public static ParkController load(Path dataDir) {
        UserManager users = new UserManager(new UserRepository(dataDir.resolve(UserRepository.FILE_NAME)));
        GroupManager groups = new GroupManager(new GroupRepository(dataDir.resolve(GroupRepository.FILE_NAME)),
                users::findByDni);
        TicketManager tickets = new TicketManager(new TicketRepository(
                dataDir.resolve(TicketRepository.TICKETS_FILE_NAME),
                dataDir.resolve(TicketRepository.GROUP_TICKETS_FILE_NAME)),
                users::findByDni, groups::find);
        AttractionManager attractions = new AttractionManager(
                new AttractionRepository(dataDir.resolve(AttractionRepository.FILE_NAME)));
        return new ParkController(users, groups, tickets, attractions);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public AttractionManager getAttractionManager() {
        return attractionManager;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    // ---- System ----

    public String setSystemDate(String text) {
        currentDate = parseDate(text);
        return "OK SYSTEM DATE -> " + currentDate;
    }

    public String setTicketPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new ParkException("invalid price " + text);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new ParkException("price must be positive with at most two decimals: " + text);
        }
        ticketPrice = price.setScale(2, RoundingMode.UNNECESSARY);
        return "OK SYSTEM PRICE -> " + ticketPrice.toPlainString();
    }

    // ---- Users ----

    public String createUser(String name, String surnames, String dni, String email,
                             String password, String phone, String card) {
        userManager.create(name, surnames, dni, email, password, phone, card);
        return "OK USER CREATED";
    }

    public String deleteUser(String email, String password) {
        User user = userManager.authenticate(email, password);
        if (ticketManager.hasActiveTicket(user, currentDate)) {
            throw new ParkException("user " + email + " has tickets dated on or after " + currentDate);
        }
        userManager.remove(user);
        groupManager.removeFromAll(user);
        return "OK USER DELETED";
    }

    public String updateUser(String email, String password, UserField field, String newValue) {
        userManager.update(email, password, field, newValue);
        return "OK USER UPDATED";
    }

    // ---- Groups ----

    public String createGroup(String id, String name) {
        groupManager.create(id, name);
        return "OK GROUP CREATED";
    }

    public String addGroupMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        groupManager.addMember(group, registeredUser(email));
        return "OK GROUP USER ADDED";
    }

    public String removeGroupMember(String groupId, String email) {
        Group group = unlockedGroup(groupId);
        groupManager.removeMember(group, registeredUser(email));
        return "OK GROUP USER REMOVED";
    }

    public String deleteGroup(String groupId) {
        groupManager.delete(unlockedGroup(groupId));
        return "OK GROUP DELETED";
    }

    // ---- Tickets ----

    public String createTicket(String id, String email, String dateText) {
        User user = registeredUser(email);
        LocalDate date = purchasableDate(dateText);
        requireNotBlankId(id);
        IndividualTicket ticket = ticketManager.createIndividual(id, user, date, ticketPrice);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    public String deleteTicket(String id) {
        if (!(ticketManager.get(id) instanceof IndividualTicket ticket)) {
            throw new ParkException(id + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (ticketManager.isInGroupTicket(ticket)) {
            throw new ParkException("ticket " + id + " belongs to a group ticket; use GROUPTICKET DELETE");
        }
        requireFuture(ticket);
        ticketManager.deleteIndividual(ticket);
        return "OK TICKET DELETED";
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        Group group = groupManager.get(groupId);
        LocalDate date = purchasableDate(dateText);
        requireNotBlankId(id);
        GroupTicket ticket = ticketManager.createGroupTicket(id, group, date, ticketPrice);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    public String deleteGroupTicket(String id) {
        if (!(ticketManager.get(id) instanceof GroupTicket ticket)) {
            throw new ParkException(id + " is not a group ticket; use TICKET DELETE");
        }
        requireFuture(ticket);
        ticketManager.deleteGroupTicket(ticket);
        return "OK GROUP TICKET DELETED";
    }

    // ---- Attractions ----

    public String createAttraction(String id, String name, String capacity) {
        attractionManager.create(id, name, capacity);
        return "OK ATTRACTION CREATED";
    }

    public String deleteAttraction(String id) {
        attractionManager.delete(attractionManager.get(id));
        return "OK ATTRACTION DELETED";
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionManager.get(attractionId);
        Ticket ticket = ticketManager.get(ticketId);
        if (!ticket.getDate().equals(currentDate)) {
            throw new ParkException("ticket " + ticketId + " is for " + ticket.getDate()
                    + ", not for the current date " + currentDate);
        }
        for (User user : ticket.getUsers()) {
            if (userManager.findByDni(user.getDni()) != user) {
                throw new ParkException("the holder " + user.getDni() + " of ticket " + ticketId + " is no longer registered");
            }
        }
        return "OK ATTRACTION ENTER -> " + attraction.enter(ticket);
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionManager.get(attractionId);
        return "OK ATTRACTION EXIT -> " + attraction.exit(ticketManager.get(ticketId));
    }

    public String attractionStatus(String attractionId) {
        Attraction attraction = attractionManager.get(attractionId);
        List<User> present = attraction.getPresent();
        StringBuilder text = new StringBuilder()
                .append("ATTRACTION ").append(attraction.getId()).append(" - ").append(attraction.getName())
                .append(System.lineSeparator()).append("  Capacity: ").append(attraction.getCapacity())
                .append(System.lineSeparator()).append("  Present: ").append(present.size())
                .append(System.lineSeparator()).append("  Available: ").append(attraction.getAvailable())
                .append(System.lineSeparator()).append("  Users present:");
        if (present.isEmpty()) {
            text.append(" none");
        }
        for (User user : present) {
            text.append(System.lineSeparator()).append("    - ").append(user.getName()).append(' ')
                    .append(user.getSurnames()).append(" (").append(user.getDni()).append(", ")
                    .append(user.getEmail()).append(')');
        }
        return text.toString();
    }

    // ---- Helpers ----

    private User registeredUser(String email) {
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new ParkException("no user with email " + email);
        }
        return user;
    }

    private Group unlockedGroup(String groupId) {
        Group group = groupManager.get(groupId);
        if (ticketManager.hasActiveGroupTicket(group, currentDate)) {
            throw new ParkException("group " + groupId + " is locked by a group ticket dated on or after " + currentDate);
        }
        return group;
    }

    private LocalDate purchasableDate(String text) {
        LocalDate date = parseDate(text);
        if (date.isBefore(currentDate)) {
            throw new ParkException("date " + date + " is before the current date " + currentDate);
        }
        return date;
    }

    private void requireFuture(Ticket ticket) {
        if (!ticket.isFutureOn(currentDate)) {
            throw new ParkException("ticket " + ticket.getId() + " can only be deleted before its date ("
                    + ticket.getDate() + "); current date is " + currentDate);
        }
    }

    private static void requireNotBlankId(String id) {
        if (!UserValidator.isNotBlank(id) || id.contains(",")) {
            throw new ParkException("ticket identifier must not be empty or contain ','");
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ParkException("invalid date " + text + ", expected yyyy-MM-dd");
        }
    }
}
