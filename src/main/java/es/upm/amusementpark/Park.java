package es.upm.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Main controller of the park. Holds the single system date and base ticket price used by every
 * business rule, and coordinates the rules that span users, groups, tickets and attractions.
 */
public class Park {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final String DOES_NOT_EXIST = " does not exist";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate systemDate;
    private BigDecimal basePrice;
    private final UserService userService;
    private final GroupService groupService;
    private final TicketService ticketService;
    private final AttractionService attractionService;

    public Park(StorageLocation storage) throws IOException {
        this.systemDate = LocalDate.now(ZoneId.systemDefault());
        this.basePrice = DEFAULT_PRICE;
        this.userService = new UserService(new UserRepository(storage.usersFile()));
        this.groupService = new GroupService(new GroupRepository(storage.groupsFile()), userService.getUsers());
        this.ticketService = new TicketService(
                new TicketRepository(storage.ticketsFile(), storage.groupTicketsFile()),
                userService.getUsers(), groupService.getGroups());
        this.attractionService = new AttractionService(new AttractionRepository(storage.attractionsFile()));
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public UserService getUserService() {
        return userService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    public AttractionService getAttractionService() {
        return attractionService;
    }

    /** Parses a strict {@code yyyy-MM-dd} date, returning {@code null} if invalid. */
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

    // System settings

    public String setSystemDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return invalidDate(text);
        }
        systemDate = date;
        return "OK: system date set to " + date;
    }

    public String setBasePrice(String text) {
        BigDecimal price = parsePrice(text.trim());
        if (price == null) {
            return "ERROR: invalid price " + text + " (expected a positive amount such as 60.00)";
        }
        basePrice = price;
        return "OK: base price set to " + formatPrice(price);
    }

    private static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        if (text.isEmpty() || dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1) {
            return null;
        }
        if (dot >= 0 && text.length() - dot - 1 > 2) {
            return null;
        }
        for (char c : text.toCharArray()) {
            if (c != '.' && (c < '0' || c > '9')) {
                return null;
            }
        }
        BigDecimal price = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        return price.signum() > 0 ? price : null;
    }

    // Users

    public String deleteUser(String email, String password) {
        User user = userService.authenticate(email, password);
        if (user == null) {
            return "ERROR: invalid email or password";
        }
        if (ticketService.hasActiveTicket(user, systemDate)) {
            return "ERROR: user " + email + " has tickets dated on or after " + systemDate + " and cannot be deleted";
        }
        String result = userService.delete(email, password);
        if (result.startsWith("OK")) {
            groupService.removeUserFromAll(user);
        }
        return result;
    }

    // Groups

    public String createGroup(String id, String name) {
        return groupService.create(id, name);
    }

    public String addToGroup(String groupId, String email) {
        Group group = groupService.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        if (ticketService.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupService.addMember(group, user);
    }

    public String removeFromGroup(String groupId, String email) {
        Group group = groupService.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        if (ticketService.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupService.removeMember(group, user);
    }

    public String deleteGroup(String groupId) {
        Group group = groupService.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        if (ticketService.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupService.delete(group);
    }

    // Tickets

    public String createTicket(String id, String email, String dateText) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return invalidDate(dateText);
        }
        return ticketService.createIndividual(id, user, date, basePrice, systemDate);
    }

    public String deleteTicket(String id) {
        return ticketService.deleteIndividual(id, systemDate);
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        Group group = groupService.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return invalidDate(dateText);
        }
        return ticketService.createGroup(id, group, date, basePrice, systemDate);
    }

    public String deleteGroupTicket(String id) {
        return ticketService.deleteGroup(id, systemDate);
    }

    // Attractions

    public String createAttraction(String id, String name, String capacity) {
        return attractionService.create(id, name, capacity);
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionService.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        Ticket ticket = ticketService.findById(ticketId);
        if (ticket == null) {
            return unknownTicket(ticketId);
        }
        return attractionService.enter(attraction, ticket, systemDate);
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionService.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        Ticket ticket = ticketService.findById(ticketId);
        if (ticket == null) {
            return unknownTicket(ticketId);
        }
        return attractionService.exit(attraction, ticket);
    }

    public String attractionStatus(String attractionId) {
        Attraction attraction = attractionService.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        return attractionService.status(attraction);
    }

    private static String invalidDate(String text) {
        return "ERROR: invalid date " + text + " (expected yyyy-MM-dd)";
    }

    private static String unknownUser(String email) {
        return "ERROR: user " + email + DOES_NOT_EXIST;
    }

    private static String unknownGroup(String id) {
        return "ERROR: group " + id + DOES_NOT_EXIST;
    }

    private static String lockedGroup(String id) {
        return "ERROR: group " + id + " has current or future group tickets and cannot be modified";
    }

    private static String unknownTicket(String id) {
        return "ERROR: ticket " + id + DOES_NOT_EXIST;
    }

    private static String unknownAttraction(String id) {
        return "ERROR: attraction " + id + DOES_NOT_EXIST;
    }
}
