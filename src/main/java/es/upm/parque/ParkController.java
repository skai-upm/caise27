package es.upm.parque;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the park. It owns the single simulated current date and the individual ticket
 * price, and coordinates operations that involve several managers.
 */
public class ParkController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TicketManager ticketManager;
    private final AttractionManager attractionManager;
    private LocalDate currentDate;
    private BigDecimal ticketPrice = new BigDecimal("40.00");

    public ParkController(UserManager userManager, GroupManager groupManager, TicketManager ticketManager,
                          AttractionManager attractionManager) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.ticketManager = ticketManager;
        this.attractionManager = attractionManager;
        this.currentDate = LocalDate.now();
    }

    /**
     * Builds a controller whose repositories store their files in the given directory.
     */
    public static ParkController load(Path dataDir) throws IOException {
        UserManager users = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        GroupManager groups = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), users);
        TicketManager tickets = new TicketManager(new TicketRepository(dataDir.resolve("tickets.txt"),
                dataDir.resolve("grouptickets.txt")), users, groups);
        AttractionManager attractions = new AttractionManager(new AttractionRepository(dataDir.resolve("attractions.txt")));
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

    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---- SYSTEM ----

    public String setDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + text;
        }
        currentDate = date;
        return "OK SYSTEM DATE -> " + currentDate;
    }

    public String setPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return "ERROR invalid price: " + text;
        }
        if (price.signum() <= 0) {
            return "ERROR price must be positive";
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return "OK SYSTEM PRICE -> " + formatPrice(ticketPrice);
    }

    // ---- USER ----

    public String createUser(String name, String surname, String dni, String email, String password,
                             String phone, String card) throws IOException {
        return userManager.create(name, surname, dni, email, password, phone, card);
    }

    public String updateUser(String email, String password, String field, String value) throws IOException {
        return userManager.update(email, password, field, value);
    }

    public String deleteUser(String email, String password) throws IOException {
        User user = userManager.authenticate(email, password);
        if (user == null) {
            return "ERROR invalid email or password";
        }
        if (ticketManager.hasActiveTicket(user, currentDate)) {
            return "ERROR user has tickets dated on or after " + currentDate;
        }
        groupManager.removeUserFromAll(user);
        return userManager.delete(email, password);
    }

    // ---- GROUP ----

    public String createGroup(String id, String name) throws IOException {
        return groupManager.create(id, name);
    }

    public String addToGroup(String groupId, String email) throws IOException {
        Group group = groupManager.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        if (ticketManager.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupManager.add(group, user);
    }

    public String removeFromGroup(String groupId, String email) throws IOException {
        Group group = groupManager.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        if (ticketManager.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupManager.remove(group, user);
    }

    public String deleteGroup(String groupId) throws IOException {
        Group group = groupManager.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        if (ticketManager.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupManager.delete(group);
    }

    private String lockedMessage(Group group) {
        return "ERROR group " + group.getId() + " has group tickets dated on or after " + currentDate;
    }

    // ---- TICKET ----

    public String createTicket(String id, String email, String dateText) throws IOException {
        if (id.isBlank()) {
            return "ERROR ticket id is required";
        }
        if (ticketManager.find(id) != null) {
            return "ERROR ticket " + id + " already exists";
        }
        User user = userManager.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + dateText;
        }
        if (date.isBefore(currentDate)) {
            return "ERROR ticket date is before the current date " + currentDate;
        }
        IndividualTicket ticket = new IndividualTicket(id, date, user, ticketPrice);
        ticketManager.add(ticket);
        return "OK TICKET CREATED -> " + formatPrice(ticket.getPrice());
    }

    public String deleteTicket(String id) throws IOException {
        Ticket ticket = ticketManager.find(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            return "ERROR ticket " + id + " does not exist";
        }
        if (individual.getGroupTicket() != null) {
            return "ERROR ticket " + id + " belongs to group ticket " + individual.getGroupTicket().getId();
        }
        if (!ticket.isDeletableOn(currentDate)) {
            return "ERROR only tickets dated after " + currentDate + " can be deleted";
        }
        ticketManager.remove(ticket);
        return "OK TICKET DELETED";
    }

    public String createGroupTicket(String id, String groupId, String dateText) throws IOException {
        if (id.isBlank()) {
            return "ERROR ticket id is required";
        }
        if (ticketManager.find(id) != null) {
            return "ERROR ticket " + id + " already exists";
        }
        Group group = groupManager.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        List<User> members = group.getUsers();
        if (members.isEmpty()) {
            return "ERROR group " + groupId + " has no members";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + dateText;
        }
        if (date.isBefore(currentDate)) {
            return "ERROR ticket date is before the current date " + currentDate;
        }
        List<String> internalIds = new ArrayList<>();
        for (int i = 1; i <= members.size(); i++) {
            String internalId = id + "-" + i;
            if (ticketManager.find(internalId) != null) {
                return "ERROR ticket " + internalId + " already exists";
            }
            internalIds.add(internalId);
        }
        GroupTicket groupTicket = new GroupTicket(id, date, group, GroupTicket.totalPrice(ticketPrice, members.size()));
        for (int i = 0; i < members.size(); i++) {
            groupTicket.addTicket(new IndividualTicket(internalIds.get(i), date, members.get(i), ticketPrice));
        }
        ticketManager.add(groupTicket);
        return "OK GROUP TICKET CREATED -> " + formatPrice(groupTicket.getPrice());
    }

    public String deleteGroupTicket(String id) throws IOException {
        Ticket ticket = ticketManager.find(id);
        if (!(ticket instanceof GroupTicket)) {
            return "ERROR group ticket " + id + " does not exist";
        }
        if (!ticket.isDeletableOn(currentDate)) {
            return "ERROR only group tickets dated after " + currentDate + " can be deleted";
        }
        ticketManager.remove(ticket);
        return "OK GROUP TICKET DELETED";
    }

    // ---- ATTRACTION ----

    public String createAttraction(String id, String name, String capacity) throws IOException {
        return attractionManager.create(id, name, capacity);
    }

    public String deleteAttraction(String id) throws IOException {
        return attractionManager.delete(id);
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionManager.find(attractionId);
        if (attraction == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        Ticket ticket = ticketManager.find(ticketId);
        if (ticket == null) {
            return "ERROR ticket " + ticketId + " does not exist";
        }
        String error = attraction.enter(ticket, currentDate);
        if (error != null) {
            return "ERROR " + error;
        }
        return "OK ATTRACTION ENTERED (" + ticket.getUsers().size() + " users)";
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionManager.find(attractionId);
        if (attraction == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        Ticket ticket = ticketManager.find(ticketId);
        if (ticket == null) {
            return "ERROR ticket " + ticketId + " does not exist";
        }
        int left = attraction.exit(ticket);
        if (left == 0) {
            return "ERROR no user of ticket " + ticketId + " is in attraction " + attractionId;
        }
        return "OK ATTRACTION EXITED (" + left + " users)";
    }

    public String attractionStatus(String attractionId) {
        Attraction a = attractionManager.find(attractionId);
        if (a == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();
        sb.append("ATTRACTION ").append(a.getId()).append(nl)
                .append("  Name: ").append(a.getName()).append(nl)
                .append("  Capacity: ").append(a.getCapacity()).append(nl)
                .append("  Present: ").append(a.getUsers().size()).append(nl)
                .append("  Available: ").append(a.getAvailable()).append(nl)
                .append("  Users:");
        if (a.getUsers().isEmpty()) {
            sb.append(" (none)");
        }
        for (User u : a.getUsers()) {
            sb.append(nl).append("    - ").append(u.getName()).append(' ').append(u.getSurname())
                    .append(" (").append(u.getEmail()).append(')');
        }
        return sb.toString();
    }
}
