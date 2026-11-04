package com.parque.business.controller;

import com.parque.business.model.Attraction;
import com.parque.business.model.AttractionType;
import com.parque.business.model.Group;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.IndividualTicket;
import com.parque.business.model.Invoice;
import com.parque.business.model.Pass;
import com.parque.business.model.SpecialPass;
import com.parque.business.model.SpeedPass;
import com.parque.business.model.Ticket;
import com.parque.business.model.User;
import com.parque.business.model.UserField;
import com.parque.business.service.AttractionService;
import com.parque.business.service.GroupInvoiceBuilder;
import com.parque.business.service.GroupService;
import com.parque.business.service.InvoiceDirector;
import com.parque.business.service.PassService;
import com.parque.business.service.TicketService;
import com.parque.business.service.UserInvoiceBuilder;
import com.parque.business.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The application's main controller. It owns the single current system date and the current individual ticket
 * price of the execution, and applies the rules that involve several entities (tickets blocking user deletion,
 * group locks, purchases at the current date and price, attraction access). Rejected operations throw
 * {@link IllegalArgumentException} with the reason.
 * <p>
 * Singleton per execution: it cannot be instantiated directly; the composition root calls
 * {@link #initialize} once at startup with the collaborators it injects, every element obtains it with
 * {@link #getInstance()}, and {@link #release()} ends the execution's instance.
 */
public class ParkController {
    /** Individual ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static ParkController instance;

    private final UserService users;
    private final GroupService groups;
    private final TicketService tickets;
    private final AttractionService attractions;
    private final PassService passes;
    private final InvoiceDirector invoiceDirector;
    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    private ParkController(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions, PassService passes, InvoiceDirector invoiceDirector, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.passes = passes;
        this.invoiceDirector = invoiceDirector;
        this.currentDate = initialDate;
    }

    /**
     * Creates the unique controller of this execution with the injected collaborators, starting at the
     * operating system's local date. Fails if the execution already has a controller.
     */
    public static synchronized ParkController initialize(UserService users, GroupService groups,
            TicketService tickets, AttractionService attractions, PassService passes, InvoiceDirector invoiceDirector) {
        return initialize(users, groups, tickets, attractions, passes, invoiceDirector, LocalDate.now());
    }

    /** As {@link #initialize(UserService, GroupService, TicketService, AttractionService, PassService, InvoiceDirector)} with an explicit start date. */
    public static synchronized ParkController initialize(UserService users, GroupService groups,
            TicketService tickets, AttractionService attractions, PassService passes, InvoiceDirector invoiceDirector,
            LocalDate initialDate) {
        if (instance != null) {
            throw new IllegalStateException("The park controller has already been initialized for this execution");
        }
        instance = new ParkController(users, groups, tickets, attractions, passes, invoiceDirector, initialDate);
        return instance;
    }

    /** The unique controller of the current execution. */
    public static synchronized ParkController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The park controller has not been initialized");
        }
        return instance;
    }

    /** Ends the current execution's controller (idempotent). */
    public static synchronized void release() {
        instance = null;
    }

    /** The single current date of the execution; every date-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setSystemDate(LocalDate date) {
        currentDate = date;
    }

    /** Price applied to individual tickets created from now on. */
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Invalid price: '" + price.toPlainString() + "' (it must be greater than zero)");
        }
        ticketPrice = price;
    }

    // ----- users

    public void createUser(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        users.create(name, surname, dni, email, password, phone, card);
    }

    /** Deletes the user unless they own a ticket dated on or after the system date; leaves their groups. */
    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (tickets.hasCurrentOrFutureTicket(user, currentDate)) {
            throw new IllegalArgumentException("User " + email + " has tickets dated on or after " + currentDate
                    + " and cannot be deleted");
        }
        User deleted = users.delete(email, password);
        groups.removeUserFromAllGroups(deleted);
    }

    public void updateUser(String email, String password, UserField field, String value) {
        users.update(email, password, field, value);
    }

    // ----- groups

    public void createGroup(String id, String name) {
        groups.create(id, name);
    }

    public void addGroupMember(String groupId, String email) {
        requireGroupUnlocked(groupId);
        groups.addMember(groupId, email);
    }

    public void removeGroupMember(String groupId, String email) {
        requireGroupUnlocked(groupId);
        groups.removeMember(groupId, email);
    }

    public void deleteGroup(String groupId) {
        requireGroupUnlocked(groupId);
        groups.delete(groupId);
    }

    /** Rejects changes to a group that has a group ticket dated on or after the system date. */
    private void requireGroupUnlocked(String groupId) {
        Group group = groups.find(groupId);
        if (group != null && tickets.isGroupLocked(group, currentDate)) {
            throw new IllegalArgumentException("Group " + groupId + " has group tickets dated on or after "
                    + currentDate + " and cannot be modified or deleted");
        }
    }

    // ----- tickets

    public IndividualTicket createTicket(String id, String email, LocalDate date) {
        return tickets.create(id, email, date, currentDate, ticketPrice);
    }

    /** Deletes a future individual ticket together with its passes. */
    public void deleteTicket(String id) {
        IndividualTicket ticket = tickets.find(id);
        tickets.delete(id, currentDate);
        passes.removePassesOf(List.of(ticket));
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        return tickets.createGroupTicket(id, groupId, date, currentDate, ticketPrice);
    }

    /** Deletes a future group ticket, its constituent tickets and their passes. */
    public void deleteGroupTicket(String id) {
        GroupTicket groupTicket = tickets.findGroupTicket(id);
        List<IndividualTicket> constituents = groupTicket == null ? List.of() : new ArrayList<>(groupTicket.getConstituents());
        tickets.deleteGroupTicket(id, currentDate);
        passes.removePassesOf(constituents);
    }

    // ----- passes

    /** Buys 1 or 10 SpeedPasses for an individual ticket; returns the created passes. */
    public List<Pass> buySpeedPasses(String purchaseId, String ticketId, String quantity) {
        return passes.buySpeedPasses(purchaseId, ticketId, quantity, currentDate);
    }

    public Pass buySpecialPass(String id, String ticketId) {
        return passes.buySpecialPass(id, ticketId, currentDate);
    }

    public void refundSpeedPass(String id) {
        passes.refund(id, SpeedPass.class, currentDate);
    }

    public void refundSpecialPass(String id) {
        passes.refund(id, SpecialPass.class, currentDate);
    }

    // ----- attractions

    public Attraction<?> createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public Attraction<?> createAttraction(String id, String name, String capacity, AttractionType type) {
        return attractions.create(id, name, capacity, type);
    }

    /** Admits the visitors of the ticket; returns how many entered. */
    public int enterAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = attractions.existing(attractionId);
        Ticket ticket = existingTicket(ticketId);
        attraction.admit(ticket, currentDate);
        return ticket.getVisitors().size();
    }

    /** Removes the visitors of the ticket that are present; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = attractions.existing(attractionId);
        return attraction.exit(existingTicket(ticketId));
    }

    public Attraction<?> attraction(String id) {
        return attractions.existing(id);
    }

    // ----- invoices

    /** Invoice of the directly purchased tickets of an existing user, built on demand. */
    public Invoice createUserInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        return invoiceDirector.construct(new UserInvoiceBuilder(user, tickets.directTicketsOf(user),
                passes.passesOfUser(user)));
    }

    /** Invoice of the group ticket purchases of an existing group, built on demand. */
    public Invoice createGroupInvoice(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            throw new IllegalArgumentException("No group with identifier " + groupId);
        }
        return invoiceDirector.construct(new GroupInvoiceBuilder(group, tickets.groupTicketsOf(group)));
    }

    private Ticket existingTicket(String id) {
        Ticket ticket = tickets.findAny(id);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket with identifier " + id);
        }
        return ticket;
    }
}
