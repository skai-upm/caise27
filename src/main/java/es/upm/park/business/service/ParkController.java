package es.upm.park.business.service;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.GroupAccess;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import java.util.ArrayList;
import java.util.List;
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
    private final PassManager passes;
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
                                                         PassManager passes, InvoiceDirector director) {
        return initialize(users, groups, tickets, attractions, passes, director, LocalDate.now());
    }

    /** As {@link #initialize(UserManager, GroupManager, TicketManager, AttractionManager, PassManager,
     *  InvoiceDirector)}
     *  with an explicit initial date. */
    public static synchronized ParkController initialize(UserManager users, GroupManager groups,
                                                         TicketManager tickets, AttractionManager attractions,
                                                         PassManager passes, InvoiceDirector director,
                                                         LocalDate initialDate) {
        if (instance != null) {
            throw new IllegalStateException("ParkController is already initialized");
        }
        instance = new ParkController(users, groups, tickets, attractions, passes, director, initialDate);
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
                           AttractionManager attractions, PassManager passes, InvoiceDirector director,
                           LocalDate initialDate) {
        this.passes = passes;
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

    /** Deletes a future standalone ticket and, first, all its passes; nothing changes on failure. */
    public void deleteTicket(String id) {
        IndividualTicket ticket = tickets.requireDeletable(id, currentDate);
        cascade(List.of(ticket), () -> tickets.delete(id, currentDate));
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        Group group = groups.find(groupId);
        requireValid(group != null, "no group with identifier " + groupId);
        return tickets.createGroupTicket(id, group, date, currentPrice, currentDate);
    }

    /**
     * Deletes a future group ticket: first the passes of its constituent tickets, then the
     * constituents together with the group ticket; nothing changes on failure.
     */
    public void deleteGroupTicket(String id) {
        GroupTicket ticket = tickets.requireGroupDeletable(id, currentDate);
        cascade(ticket.getConstituents(), () -> tickets.deleteGroupTicket(id, currentDate));
    }

    /** Removes the passes of {@code owners}, then runs {@code deletion}; compensates on failure. */
    private void cascade(List<IndividualTicket> owners, Runnable deletion) {
        List<Pass> before = passes.snapshot();
        passes.removeFor(owners);
        try {
            deletion.run();
        } catch (RuntimeException e) {
            passes.restore(before);
            throw e;
        }
    }

    // --- invoices

    /** Builds, on demand, the invoice of the registered user with that email. */
    public Invoice createUserInvoice(String email) {
        User user = users.findByEmail(email);
        requireValid(user != null, "no registered user with email " + email);
        List<Pass> userPasses = new ArrayList<>();
        for (Pass p : passes.getPasses()) {
            if (p.getTicket() != null && p.getTicket().isOwnedBy(user)) {
                userPasses.add(p);
            }
        }
        return director.construct(new UserInvoiceBuilder(user, tickets.getTickets(), userPasses));
    }

    /** Builds, on demand, the invoice of the group with that identifier. */
    public Invoice createGroupInvoice(String groupId) {
        Group group = groups.find(groupId);
        requireValid(group != null, "no group with identifier " + groupId);
        return director.construct(new GroupInvoiceBuilder(group, tickets.getGroupTickets()));
    }

    // --- passes

    /** Buys 1 or 10 SpeedPasses for an individual ticket (standalone or constituent). */
    public List<Pass> buySpeedPasses(String id, String ticketId, int quantity) {
        return passes.buySpeedPasses(id, requireIndividualTicket(ticketId), quantity);
    }

    public Pass buySpecialPass(String id, String ticketId) {
        return passes.buySpecialPass(id, requireIndividualTicket(ticketId));
    }

    public void refundSpeedPass(String id) {
        passes.refund(id, SpeedPass.KIND, currentDate);
    }

    public void refundSpecialPass(String id) {
        passes.refund(id, SpecialPass.KIND, currentDate);
    }

    private IndividualTicket requireIndividualTicket(String ticketId) {
        IndividualTicket t = tickets.findIndividualTicket(ticketId);
        requireValid(t != null, "no individual ticket with identifier " + ticketId);
        return t;
    }

    // --- attractions

    public Attraction<?> createAttraction(String id, String name, int capacity, AttractionType type) {
        return attractions.create(id, name, capacity, type);
    }

    /**
     * Admits the holders of a ticket valid for the system date. With {@code speed} one unused
     * SpeedPass per individual ticket involved is consumed; at SPECIAL attractions the ticket's
     * unused Special Pass is consumed. Either everything happens or nothing does.
     */
    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = findAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        requireValid(attraction.accepts(ticket), "attraction " + attractionId + " (" + attraction.type()
                + ") does not accept " + (ticket instanceof GroupAccess ? "group" : "individual") + " tickets");
        List<Pass> selected = selectPasses(attraction, ticket, speed);
        attraction.enterAny(ticket, currentDate);
        try {
            passes.consume(selected);
        } catch (RuntimeException e) {
            attraction.exitAny(ticket);
            throw e;
        }
        return new EntryResult(attraction, ticket.getHolders().size(), selected);
    }

    /** Preflight: the passes an entry will consume, without changing anything. */
    private List<Pass> selectPasses(Attraction<?> attraction, Ticket ticket, boolean speed) {
        List<Pass> selected = new ArrayList<>();
        if (attraction.type() == AttractionType.SPECIAL) {
            requireValid(!speed, "the SPEED option is not accepted at SPECIAL attraction " + attraction.getId());
            IndividualTicket individual = (IndividualTicket) ticket;
            Pass special = passes.unusedSpecialPass(individual);
            requireValid(special != null, "ticket " + ticket.getId() + " has no unused Special Pass");
            selected.add(special);
        } else if (speed) {
            for (IndividualTicket t : individualTicketsOf(ticket)) {
                Pass speedPass = passes.firstUnusedSpeedPass(t);
                requireValid(speedPass != null, "ticket " + t.getId() + " has no unused SpeedPass");
                selected.add(speedPass);
            }
        }
        return selected;
    }

    private static List<IndividualTicket> individualTicketsOf(Ticket ticket) {
        if (ticket instanceof GroupAccess group) {
            return group.getConstituents();
        }
        return List.of((IndividualTicket) ticket);
    }

    /** Removes the holders of a ticket from an attraction; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        return findAttraction(attractionId).exitAny(findTicket(ticketId));
    }

    public Attraction<?> findAttraction(String id) {
        Attraction<?> a = attractions.find(id);
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
