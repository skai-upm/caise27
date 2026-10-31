package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.UserRepository;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Main controller of the park. It holds the single system date and the base
 * ticket price used by every rule, owns the managers, and enforces the rules
 * that span several of them (user deletion and group locking).
 */
public class ParkController {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private LocalDate systemDate;
    private BigDecimal basePrice = DEFAULT_PRICE;

    public ParkController(StoragePaths paths, LocalDate startDate) throws IOException {
        this.systemDate = startDate;
        this.users = new UserManager(new UserRepository(paths.users()));
        this.groups = new GroupManager(new GroupRepository(paths.groups()), users);
        this.tickets = new TicketManager(new TicketRepository(paths), users::findByDni, groups::find);
        this.attractions = new AttractionManager(new AttractionRepository(paths.attractions()));
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

    public void setSystemDate(String value) {
        systemDate = parseDate(value);
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String value) {
        BigDecimal price;
        try {
            price = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid price '" + value + "'");
        }
        require(price.signum() >= 0, "price must not be negative");
        basePrice = price.setScale(2, RoundingMode.HALF_UP);
    }

    /** Parses a strict {@code yyyy-MM-dd} calendar date. */
    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date '" + value + "', expected yyyy-MM-dd");
        }
    }

    /**
     * Deletes an authenticated user unless it holds a current or future ticket.
     * The user leaves its groups and any attraction; historical tickets remain.
     */
    public void deleteUser(String email, String password) throws IOException {
        User user = users.authenticate(email, password);
        require(!tickets.hasActiveTicket(user, systemDate),
                "user " + email + " has tickets dated on or after " + systemDate);
        users.remove(user);
        groups.removeFromAll(user);
        attractions.removeFromAll(user);
    }

    public void createGroup(String id, String name) throws IOException {
        groups.create(id, name);
    }

    public void addToGroup(String groupId, String email) throws IOException {
        Group group = unlockedGroup(groupId);
        groups.addMember(group, registeredUser(email));
    }

    public void removeFromGroup(String groupId, String email) throws IOException {
        Group group = unlockedGroup(groupId);
        groups.removeMember(group, registeredUser(email));
    }

    public void deleteGroup(String groupId) throws IOException {
        groups.delete(unlockedGroup(groupId));
    }

    public IndividualTicket createTicket(String id, String email, String date) throws IOException {
        return tickets.createIndividual(id, registeredUser(email), parseDate(date), basePrice, systemDate);
    }

    public void deleteTicket(String id) throws IOException {
        tickets.deleteIndividual(id, systemDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) throws IOException {
        return tickets.createGroup(id, groups.get(groupId), parseDate(date), basePrice, systemDate);
    }

    public void deleteGroupTicket(String id) throws IOException {
        tickets.deleteGroup(id, systemDate);
    }

    public void createAttraction(String id, String name, String capacity) throws IOException {
        attractions.create(id, name, capacity);
    }

    public void enterAttraction(String attractionId, String ticketId) {
        attractions.enter(attractions.get(attractionId), tickets.get(ticketId), systemDate);
    }

    public int exitAttraction(String attractionId, String ticketId) {
        return attractions.exit(attractions.get(attractionId), tickets.get(ticketId));
    }

    private User registeredUser(String email) {
        User user = users.findByEmail(email);
        require(user != null, "user " + email + " does not exist");
        return user;
    }

    private Group unlockedGroup(String groupId) {
        Group group = groups.get(groupId);
        require(!tickets.isLocked(group, systemDate),
                "group " + groupId + " has group tickets dated on or after " + systemDate);
        return group;
    }
}
