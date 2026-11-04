package es.upm.park.business.service;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Main business controller. Owns the single current system date and the current
 * individual-ticket price, and applies the rules that involve several managers.
 * Receives already-parsed values from the presentation layer.
 */
public class ParkController {

    /** Individual-ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private final InvoiceDirector director;
    /** The single current date of the application; time-dependent rules must use it. */
    private LocalDate currentDate;
    /** Current individual-ticket price; never persisted. */
    private BigDecimal currentPrice = DEFAULT_PRICE;

    private static ParkController instance;

    /**
     * Creates the single controller of this execution with its injected collaborators and
     * the operating system's local date as current date; fails if it already exists.
     */
    public static synchronized ParkController initialize(UserManager users, GroupManager groups,
                                                         TicketManager tickets, AttractionManager attractions,
                                                         InvoiceDirector director) {
        return initialize(users, groups, tickets, attractions, director, LocalDate.now());
    }

    /** As {@link #initialize(UserManager, GroupManager, TicketManager, AttractionManager, InvoiceDirector)}
     *  with an explicit initial date. */
    public static synchronized ParkController initialize(UserManager users, GroupManager groups,
                                                         TicketManager tickets, AttractionManager attractions,
                                                         InvoiceDirector director, LocalDate initialDate) {
        if (instance != null) {
            throw new IllegalStateException("ParkController is already initialized");
        }
        instance = new ParkController(users, groups, tickets, attractions, director, initialDate);
        return instance;
    }

    /** The single controller of this execution; fails if not initialized. */
    public static synchronized ParkController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ParkController is not initialized");
        }
        return instance;
    }

    /** Ends the current execution's controller so a new one can be initialized. */
    public static synchronized void reset() {
        instance = null;
    }

    private ParkController(UserManager users, GroupManager groups, TicketManager tickets,
                           AttractionManager attractions, InvoiceDirector director, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.director = director;
        this.currentDate = initialDate;
    }

    // --- system configuration

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** Sets the individual-ticket price; it must be greater than zero. */
    public void setCurrentPrice(BigDecimal price) {
        requireValid(price.signum() > 0, "invalid price '" + price.toPlainString()
                + "', expected a positive amount such as 60.00");
        currentPrice = price;
    }

    // --- users

    public void createUser(String name, String surnames, String dni, String email,
                           String password, String phone, String card) {
        users.create(name, surnames, dni, email, password, phone, card);
    }

    /** Deletes a user unless it holds a ticket dated on or after the system date. */
    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        requireValid(!tickets.hasTicketOnOrAfter(user, currentDate),
                "user " + email + " has tickets dated on or after " + currentDate);
        User removed = users.delete(email, password);
        groups.removeUserFromAllGroups(removed);
    }

    public void updateUser(String email, String password, UserField field, String value) {
        users.update(email, password, field, value);
    }

    // --- groups

    public void createGroup(String id, String name) {
        groups.create(id, name);
    }

    public void addGroupMember(String groupId, String email) {
        requireUnlocked(groupId);
        groups.addMember(groupId, email);
    }

    public void removeGroupMember(String groupId, String email) {
        requireUnlocked(groupId);
        groups.removeMember(groupId, email);
    }

    public void deleteGroup(String groupId) {
        requireUnlocked(groupId);
        groups.delete(groupId);
    }

    /** A group with a current or future group ticket cannot be modified or deleted. */
    private void requireUnlocked(String groupId) {
        Group group = groups.find(groupId);
        requireValid(group == null || !tickets.hasGroupTicketOnOrAfter(group, currentDate),
                "group " + groupId + " is locked by a group ticket dated on or after " + currentDate);
    }

    // --- tickets

    public IndividualTicket createTicket(String id, String email, LocalDate date) {
        User owner = users.findByEmail(email);
        requireValid(owner != null, "no registered user with email " + email);
        return tickets.create(id, owner, date, currentPrice, currentDate);
    }

    public void deleteTicket(String id) {
        tickets.delete(id, currentDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        Group group = groups.find(groupId);
        requireValid(group != null, "no group with identifier " + groupId);
        return tickets.createGroupTicket(id, group, date, currentPrice, currentDate);
    }

    public void deleteGroupTicket(String id) {
        tickets.deleteGroupTicket(id, currentDate);
    }

    // --- invoices

    /** Builds, on demand, the invoice of the registered user with that email. */
    public Invoice createUserInvoice(String email) {
        User user = users.findByEmail(email);
        requireValid(user != null, "no registered user with email " + email);
        return director.construct(new UserInvoiceBuilder(user, tickets.getTickets()));
    }

    /** Builds, on demand, the invoice of the group with that identifier. */
    public Invoice createGroupInvoice(String groupId) {
        Group group = groups.find(groupId);
        requireValid(group != null, "no group with identifier " + groupId);
        return director.construct(new GroupInvoiceBuilder(group, tickets.getGroupTickets()));
    }

    // --- attractions

    public Attraction createAttraction(String id, String name, int capacity) {
        return attractions.create(id, name, capacity);
    }

    /** Admits the holders of a ticket valid for the system date; returns the attraction. */
    public Attraction enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = findAttraction(attractionId);
        attraction.enter(findTicket(ticketId), currentDate);
        return attraction;
    }

    /** Removes the holders of a ticket from an attraction; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        return findAttraction(attractionId).exit(findTicket(ticketId));
    }

    public Attraction findAttraction(String id) {
        Attraction a = attractions.find(id);
        requireValid(a != null, "no attraction with identifier " + id);
        return a;
    }

    public Ticket findTicket(String id) {
        Ticket t = tickets.findAnyTicket(id);
        requireValid(t != null, "no ticket with identifier " + id);
        return t;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
