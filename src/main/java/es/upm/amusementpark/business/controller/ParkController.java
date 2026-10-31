package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceService;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Main controller of the park. It holds the single system date and the base
 * ticket price used by every rule, owns the managers, and enforces the rules
 * that span several of them (user deletion and group locking).
 */
public class ParkController {

    private static final SingletonSlot<ParkController> INSTANCE = new SingletonSlot<>("ParkController");

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private final InvoiceService invoices;
    private LocalDate systemDate;
    private BigDecimal basePrice = DEFAULT_PRICE;

    private ParkController(UserManager users, GroupManager groups, TicketManager tickets,
                           AttractionManager attractions, InvoiceService invoices, LocalDate startDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoices = invoices;
        this.systemDate = startDate;
    }

    /**
     * Creates the single park controller for this execution. Its collaborating
     * controllers and services are created at startup and injected here.
     */
    public static ParkController initialize(UserManager users, GroupManager groups, TicketManager tickets,
                                            AttractionManager attractions, InvoiceService invoices,
                                            LocalDate startDate) throws IOException {
        return INSTANCE.initialize(() -> new ParkController(users, groups, tickets, attractions, invoices, startDate));
    }

    /** Returns the instance created by {@link #initialize}. */
    public static ParkController getInstance() {
        return INSTANCE.get();
    }

    /** Discards the instance at the end of an execution. */
    public static void reset() {
        INSTANCE.reset();
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

    public void createAttraction(String id, String name, String capacity, String type) throws IOException {
        attractions.create(id, name, capacity, type);
    }

    /**
     * Lets a ticket enter an attraction, optionally with SpeedPasses, and
     * returns the passes consumed. If their new state cannot be saved the
     * entry is undone.
     */
    public List<Pass> enterAttraction(String attractionId, String ticketId, boolean speed) throws IOException {
        Attraction<?> attraction = attractions.get(attractionId);
        Ticket ticket = tickets.get(ticketId);
        List<Pass> consumed = attractions.enter(attraction, ticket, systemDate, speed);
        if (!consumed.isEmpty()) {
            try {
                tickets.savePassStates();
            } catch (IOException e) {
                ticket.getHolders().forEach(attraction::leave);
                consumed.forEach(Pass::release);
                throw e;
            }
        }
        return consumed;
    }

    public int exitAttraction(String attractionId, String ticketId) {
        return attractions.exit(attractions.get(attractionId), tickets.get(ticketId));
    }

    public List<SpeedPass> buySpeedPasses(String ticketId, String quantity) throws IOException {
        return tickets.buySpeedPasses(ticketId, quantity, systemDate);
    }

    public SpecialPass buySpecialPass(String ticketId) throws IOException {
        return tickets.buySpecialPass(ticketId, systemDate);
    }

    public void deleteSpeedPass(String passId) throws IOException {
        tickets.deleteSpeedPass(passId, systemDate);
    }

    public void deleteSpecialPass(String passId) throws IOException {
        tickets.deleteSpecialPass(passId, systemDate);
    }

    /** Builds an invoice for {@code USER;<email>} or {@code GROUP;<groupId>}. */
    public Invoice createInvoice(String recipientType, String id) {
        switch (recipientType) {
            case "USER" -> {
                User user = registeredUser(id);
                return invoices.userInvoice(user, tickets.directTicketsOf(user), tickets.ticketsHeldBy(user));
            }
            case "GROUP" -> {
                Group group = groups.get(id);
                return invoices.groupInvoice(group, tickets.groupTicketsOf(group));
            }
            default -> throw new IllegalArgumentException("invoice recipient must be USER or GROUP");
        }
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
