package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.Invoice;
import es.upm.etsisi.amusementpark.business.model.ParkTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The application's main controller. It holds the single current system date and the current ticket price
 * of the execution (neither is persisted) and applies the business rules that involve several controllers:
 * user deletion blocked by valid tickets, groups locked by valid group tickets, ticket dates and prices, and
 * attraction access. Rule violations are reported with {@link IllegalArgumentException}.
 */
public class ParkController {

    /** Ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final int PRICE_DECIMALS = 2;

    private final UserController users;
    private final GroupController groups;
    private final TicketController tickets;
    private final AttractionController attractions;
    private final PassController passes;
    private LocalDate currentDate;
    private BigDecimal currentPrice = DEFAULT_PRICE;

    private static volatile ParkController instance;

    /**
     * Creates the unique main controller of the current execution, with the operating system's local date
     * as current date and the injected controllers it collaborates with. Called once by the composition root.
     */
    public static ParkController initialize(UserController users, GroupController groups, TicketController tickets,
                                            AttractionController attractions, PassController passes) {
        return initialize(users, groups, tickets, attractions, passes, LocalDate.now());
    }

    /** Same as the other {@code initialize} with a given start date. */
    public static synchronized ParkController initialize(UserController users, GroupController groups,
                                                         TicketController tickets, AttractionController attractions,
                                                         PassController passes, LocalDate initialDate) {
        instance = new ParkController(users, groups, tickets, attractions, passes, initialDate);
        return instance;
    }

    /** The unique instance of the current execution. */
    public static ParkController getInstance() {
        ParkController current = instance;
        if (current == null) {
            throw new IllegalStateException("ParkController has not been initialized");
        }
        return current;
    }

    private ParkController(UserController users, GroupController groups, TicketController tickets,
                           AttractionController attractions, PassController passes, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.passes = passes;
        this.currentDate = initialDate;
    }

    public UserController users() {
        return users;
    }

    public GroupController groups() {
        return groups;
    }

    public TicketController tickets() {
        return tickets;
    }

    public AttractionController attractions() {
        return attractions;
    }

    public PassController passes() {
        return passes;
    }

    /** The single current date of the execution; every time-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    /** The current ticket price, applied to tickets created from now on. */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** Sets the price: a non-negative amount with at most two decimals. */
    public void setCurrentPrice(BigDecimal price, String text) {
        if (price.signum() < 0 || price.scale() > PRICE_DECIMALS) {
            throw new IllegalArgumentException(
                    "Invalid price " + text + ": it must be a non-negative amount with at most two decimals");
        }
        this.currentPrice = price.setScale(PRICE_DECIMALS);
    }

    /** Deletes an authenticated user unless it has tickets dated on or after the system date. */
    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (tickets.hasActiveTickets(user, currentDate)) {
            throw new IllegalArgumentException(
                    "User " + email + " cannot be deleted: it has tickets dated on or after " + currentDate);
        }
        User deleted = users.delete(email, password);
        groups.removeUserFromAll(deleted);
    }

    public void addMember(String groupId, String email) {
        requireUnlocked(groupId);
        groups.addMember(groupId, email);
    }

    public void removeMember(String groupId, String email) {
        requireUnlocked(groupId);
        groups.removeMember(groupId, email);
    }

    public void deleteGroup(String groupId) {
        requireUnlocked(groupId);
        groups.delete(groupId);
    }

    public Ticket createTicket(String id, String email, LocalDate date) {
        return tickets.create(id, email, date, currentDate, currentPrice);
    }

    public void deleteTicket(String id) {
        tickets.delete(id, currentDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        Group group = groups.find(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No group with identifier " + groupId));
        return tickets.createGroupTicket(id, group, date, currentDate, currentPrice);
    }

    public void deleteGroupTicket(String id) {
        tickets.deleteGroupTicket(id, currentDate);
    }

    /** Admits the users of an individual or group ticket valid for the system date. */
    public ParkTicket enter(String attractionId, String ticketId) {
        ParkTicket ticket = tickets.findUsable(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("No ticket with identifier " + ticketId));
        attractions.enter(attractionId, ticket, currentDate);
        return ticket;
    }

    /** Exit by ticket identifier or, as a fallback, by the email of a present user (no date check). */
    public Attraction<?> exit(String attractionId, String ticketOrEmail) {
        Attraction<?> attraction = attractions.get(attractionId);
        List<User> leaving = tickets.findUsable(ticketOrEmail).map(ParkTicket::getVisitors)
                .or(() -> users.findByEmail(ticketOrEmail).map(List::of))
                .orElseThrow(() -> new IllegalArgumentException("No ticket or user " + ticketOrEmail));
        return attractions.exit(attraction.getId(), leaving);
    }

    /** Buys 1 or 10 SpeedPasses for an individual ticket dated on or after the system date. */
    public List<Pass> buySpeedPasses(String id, String ticketId, int quantity) {
        return passes.buySpeedPasses(id, ticketId, quantity, currentDate);
    }

    /** Buys the Special Pass of an individual ticket dated on or after the system date. */
    public Pass buySpecialPass(String id, String ticketId) {
        return passes.buySpecialPass(id, ticketId, currentDate);
    }

    /** Refunds an unconsumed pass of that type whose ticket is dated after the system date. */
    public Pass refundPass(String type, String passId) {
        return passes.refund(type, passId, currentDate);
    }

    /** Invoice of a registered user: tickets bought directly for that user and the passes of all its tickets. */
    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registered user with email " + email));
        List<Ticket> direct = tickets.getTickets().stream().filter(t -> t.belongsTo(user)).toList();
        List<Pass> personal = passes.getPasses().stream().filter(p -> p.getTicket().belongsTo(user)).toList();
        return InvoiceDirector.construct(new UserInvoiceBuilder(user, direct, personal), currentDate);
    }

    /** Invoice of an existing group: its group tickets with their totals. */
    public Invoice groupInvoice(String groupId) {
        Group group = groups.find(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No group with identifier " + groupId));
        List<GroupTicket> bought = tickets.getGroupTickets().stream().filter(t -> t.belongsTo(group)).toList();
        return InvoiceDirector.construct(new GroupInvoiceBuilder(group, bought), currentDate);
    }

    /** Rejects changes to a group that has a group ticket dated on or after the system date. */
    private void requireUnlocked(String groupId) {
        groups.find(groupId).filter(g -> tickets.isGroupLocked(g, currentDate)).ifPresent(g -> {
            throw new IllegalArgumentException("Group " + groupId + " is locked by a group ticket dated on or after "
                    + currentDate);
        });
    }
}
