package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.PassManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main controller of the park (Singleton: one instance per execution). It owns
 * the run-wide system date and ticket price and carries out every business
 * operation, including the rules that involve several kinds of entity (user
 * deletion, group locks, attraction access). It knows nothing about the text
 * command format. Its collaborators are injected by {@link #initialize}.
 */
public final class ParkController {

    private static ParkController instance;

    /** Price of new individual tickets at the start of every run. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private final PassManager passes;
    private final InvoiceDirector invoiceDirector;
    /** The single simulated current date of this run; never persisted. */
    private LocalDate currentDate;
    /** Price of new individual tickets; never persisted. */
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    private ParkController(ParkServices services, InvoiceDirector invoiceDirector, LocalDate startupDate) {
        this.users = services.users();
        this.groups = services.groups();
        this.tickets = services.tickets();
        this.attractions = services.attractions();
        this.passes = services.passes();
        this.invoiceDirector = invoiceDirector;
        this.currentDate = startupDate;
    }

    /**
     * Creates the single controller of this execution with the operating system's
     * local date as current date.
     *
     * @throws IllegalStateException if it was already created and not shut down
     */
    public static ParkController initialize(ParkServices services, InvoiceDirector invoiceDirector) {
        return initialize(services, invoiceDirector, LocalDate.now());
    }

    /**
     * Creates the single controller of this execution.
     *
     * @throws IllegalStateException if it was already created and not shut down
     */
    public static synchronized ParkController initialize(ParkServices services, InvoiceDirector invoiceDirector,
                                                         LocalDate startupDate) {
        if (instance != null) {
            throw new IllegalStateException("The park controller has already been initialized for this execution");
        }
        instance = new ParkController(services, invoiceDirector, startupDate);
        return instance;
    }

    /**
     * The controller of the current execution.
     *
     * @throws IllegalStateException if it has not been initialized
     */
    public static synchronized ParkController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The park controller has not been initialized");
        }
        return instance;
    }

    /** Releases the controller at the end of an execution. */
    public static synchronized void shutdown() {
        instance = null;
    }

    // ---------- System settings ----------

    /** The current system date that all time-dependent operations must use. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    /** The price that the next individual ticket will be charged. */
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal price) throws UserException {
        if (price == null || price.signum() <= 0) {
            throw new UserException("The ticket price must be positive");
        }
        this.ticketPrice = price;
    }

    // ---------- Users ----------

    public void createUser(String name, String surname, String dni, String email,
                           String password, String phone, String card) throws UserException {
        users.create(name, surname, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, UserField field, String value) throws UserException {
        users.update(email, password, field, value);
    }

    /** Deletes a user without current or future tickets, and removes them from tickets, attractions and groups. */
    public void deleteUser(String email, String password) throws UserException {
        User user = users.authenticate(email, password);
        if (tickets.hasTicketOnOrAfter(user, currentDate)) {
            throw new UserException("User " + email + " has a ticket dated on or after " + currentDate
                    + " and cannot be deleted");
        }
        User deleted = users.delete(email, password);
        tickets.detachUser(deleted);
        attractions.removeUserEverywhere(deleted);
        groups.removeUserEverywhere(deleted);
    }

    // ---------- Groups ----------

    public void createGroup(String id, String name) throws UserException {
        groups.create(id, name);
    }

    public void addGroupMember(String groupId, String email) throws UserException {
        requireGroupUnlocked(groupId);
        groups.addMember(groupId, email);
    }

    public void removeGroupMember(String groupId, String email) throws UserException {
        requireGroupUnlocked(groupId);
        groups.removeMember(groupId, email);
    }

    public void deleteGroup(String groupId) throws UserException {
        requireGroupUnlocked(groupId);
        Optional<Group> group = groups.find(groupId);
        groups.delete(groupId);
        group.ifPresent(tickets::detachGroup);
    }

    public Group requireGroup(String groupId) throws UserException {
        return groups.find(groupId).orElseThrow(() -> new UserException("No group with id " + groupId));
    }

    /** A group with a group ticket dated on or after the system date cannot be modified or deleted. */
    private void requireGroupUnlocked(String groupId) throws UserException {
        Optional<Group> group = groups.find(groupId);
        if (group.isPresent() && tickets.groupHasTicketOnOrAfter(group.get(), currentDate)) {
            throw new UserException("Group " + groupId + " has a group ticket dated on or after " + currentDate
                    + " and cannot be modified or deleted");
        }
    }

    // ---------- Tickets ----------

    public IndividualTicket createTicket(String id, String email, LocalDate date) throws UserException {
        return tickets.create(id, email, date, currentDate, ticketPrice);
    }

    /** Deletes a future individual ticket and, first, all its passes; nothing is removed on failure. */
    public void deleteTicket(String id) throws UserException {
        IndividualTicket ticket = tickets.checkDeletable(id, currentDate);
        cascade(passes.passesOf(ticket), () -> tickets.delete(id, currentDate));
    }

    public GroupTicket createGroupTicket(String id, Group group, LocalDate date) throws UserException {
        return tickets.createGroupTicket(id, group, date, currentDate, ticketPrice);
    }

    /**
     * Deletes a future group ticket: first the passes of its individual tickets,
     * then those tickets together with the group ticket; nothing is removed on failure.
     */
    public void deleteGroupTicket(String id) throws UserException {
        GroupTicket groupTicket = tickets.checkGroupDeletable(id, currentDate);
        List<Pass> memberPasses = new ArrayList<>();
        groupTicket.getParts().forEach(part -> memberPasses.addAll(passes.passesOf(part)));
        cascade(memberPasses, () -> tickets.deleteGroupTicket(id, currentDate));
    }

    /** Removes the passes, then runs the ticket deletion; restores the passes if it fails. */
    private void cascade(List<Pass> ownedPasses, TicketDeletion deletion) throws UserException {
        Map<String, Pass> snapshot = passes.removeAll(ownedPasses);
        try {
            deletion.run();
        } catch (UserException e) {
            passes.restore(snapshot);
            throw e;
        }
    }

    @FunctionalInterface
    private interface TicketDeletion {
        void run() throws UserException;
    }

    // ---------- Passes ----------

    /** Buys 1 or 10 SpeedPasses for an individual ticket; returns the total charged. */
    public BigDecimal buySpeedPasses(String purchaseId, String ticketId, String quantity) throws UserException {
        return passes.buySpeedPasses(purchaseId, ticketId, quantity, currentDate);
    }

    /** Buys the Special Pass of an individual ticket; returns the price charged. */
    public BigDecimal buySpecialPass(String passId, String ticketId) throws UserException {
        return passes.buySpecialPass(passId, ticketId, currentDate);
    }

    public void refundSpeedPass(String passId) throws UserException {
        passes.refund(passId, SpeedPass.class, currentDate);
    }

    public void refundSpecialPass(String passId) throws UserException {
        passes.refund(passId, SpecialPass.class, currentDate);
    }

    // ---------- Invoices ----------

    /** Invoice of the tickets bought directly for the user (not those generated by group tickets). */
    public Invoice createUserInvoice(String email) throws UserException {
        User user = users.findByEmail(email).orElseThrow(() -> new UserException("No user with email " + email));
        List<IndividualTicket> direct = tickets.getTickets().stream().filter(t -> t.belongsTo(user)).toList();
        return invoiceDirector.construct(new UserInvoiceBuilder(user, direct, passes.passesOfUser(user)));
    }

    /** Invoice of the group tickets bought by the group. */
    public Invoice createGroupInvoice(String groupId) throws UserException {
        Group group = requireGroup(groupId);
        List<GroupTicket> bought = tickets.getGroupTickets().stream().filter(t -> t.belongsTo(group)).toList();
        return invoiceDirector.construct(new GroupInvoiceBuilder(group, bought));
    }

    // ---------- Attractions ----------

    /** Creates an attraction; a null {@code type} means GENERAL. */
    public void createAttraction(String id, String name, String capacity, String type) throws UserException {
        attractions.create(id, name, capacity, type);
    }

    public Attraction<?> getAttraction(String id) throws UserException {
        return attractions.require(id);
    }

    /** Admits the visitors of a ticket into an attraction and returns the ticket used. */
    /**
     * Admits the visitors of a ticket, consuming SpeedPasses when {@code speed} is
     * requested and the Special Pass at SPECIAL attractions. Every condition is
     * checked first; a failed entry changes neither occupancy nor passes.
     */
    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) throws UserException {
        Attraction<?> attraction = attractions.require(attractionId);
        Ticket ticket = requireTicket(ticketId);
        boolean special = attraction.getType() == AttractionType.SPECIAL;
        if (special && speed) {
            throw new UserException("Attraction " + attractionId + " (SPECIAL) does not accept the SPEED option");
        }
        attraction.checkAdmission(ticket, currentDate);
        List<Pass> toConsume = new ArrayList<>();
        if (speed) {
            for (IndividualTicket t : individualTicketsOf(ticket)) {
                toConsume.add(passes.firstUnusedSpeedPass(t)
                        .orElseThrow(() -> new UserException("Ticket " + t.getId() + " has no unused SpeedPass")));
            }
        } else if (special) {
            IndividualTicket t = (IndividualTicket) ticket;
            toConsume.add(passes.unusedSpecialPass(t)
                    .orElseThrow(() -> new UserException("Ticket " + t.getId() + " has no unused Special Pass")));
        }
        attraction.admit(ticket, currentDate);
        if (!toConsume.isEmpty()) {
            try {
                passes.consume(toConsume);
            } catch (UserException e) {
                ticket.getVisitors().forEach(attraction::remove);
                throw e;
            }
        }
        return new EntryResult(ticket, List.copyOf(toConsume));
    }

    private static List<IndividualTicket> individualTicketsOf(Ticket ticket) {
        if (ticket instanceof GroupTicket group) {
            return group.getParts();
        }
        return List.of((IndividualTicket) ticket);
    }

    public void exitAttraction(String attractionId, String ticketId) throws UserException {
        Attraction<?> attraction = attractions.require(attractionId);
        attraction.exit(requireTicket(ticketId));
    }

    private Ticket requireTicket(String id) throws UserException {
        return tickets.findAnyTicket(id).orElseThrow(() -> new UserException("No ticket with id " + id));
    }
}
