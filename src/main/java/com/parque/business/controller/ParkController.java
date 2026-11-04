package com.parque.business.controller;

import com.parque.business.model.Attraction;
import com.parque.business.model.Group;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.IndividualTicket;
import com.parque.business.model.Invoice;
import com.parque.business.model.Ticket;
import com.parque.business.model.User;
import com.parque.business.model.UserField;
import com.parque.business.service.AttractionService;
import com.parque.business.service.GroupInvoiceBuilder;
import com.parque.business.service.GroupService;
import com.parque.business.service.InvoiceDirector;
import com.parque.business.service.TicketService;
import com.parque.business.service.UserInvoiceBuilder;
import com.parque.business.service.UserService;
import com.parque.persistence.AttractionRepository;
import com.parque.persistence.GroupRepository;
import com.parque.persistence.GroupTicketRepository;
import com.parque.persistence.TicketRepository;
import com.parque.persistence.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * The application's main controller. It owns the single current system date and the current individual ticket
 * price of the execution, and applies the rules that involve several entities (tickets blocking user deletion,
 * group locks, purchases at the current date and price, attraction access). Rejected operations throw
 * {@link IllegalArgumentException} with the reason.
 */
public class ParkController {
    /** Individual ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String GROUP_TICKETS_FILE = "grouptickets.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private final UserService users;
    private final GroupService groups;
    private final TicketService tickets;
    private final AttractionService attractions;
    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    /** Creates the controller with the operating system's local date as the current date. */
    public ParkController(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions) {
        this(users, groups, tickets, attractions, LocalDate.now());
    }

    public ParkController(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.currentDate = initialDate;
    }

    /** Builds the services over the data files of {@code dataDir} and loads the stored state. */
    public static ParkController open(Path dataDir) throws IOException {
        UserService users = new UserService(new UserRepository(dataDir.resolve(USERS_FILE)));
        GroupService groups = new GroupService(new GroupRepository(dataDir.resolve(GROUPS_FILE)), users);
        TicketService tickets = new TicketService(new TicketRepository(dataDir.resolve(TICKETS_FILE)),
                new GroupTicketRepository(dataDir.resolve(GROUP_TICKETS_FILE)), users, groups);
        AttractionService attractions = new AttractionService(new AttractionRepository(dataDir.resolve(ATTRACTIONS_FILE)));
        return new ParkController(users, groups, tickets, attractions);
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

    public void deleteTicket(String id) {
        tickets.delete(id, currentDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        return tickets.createGroupTicket(id, groupId, date, currentDate, ticketPrice);
    }

    public void deleteGroupTicket(String id) {
        tickets.deleteGroupTicket(id, currentDate);
    }

    // ----- attractions

    public Attraction createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    /** Admits the visitors of the ticket; returns how many entered. */
    public int enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.existing(attractionId);
        Ticket ticket = existingTicket(ticketId);
        attraction.enter(ticket, currentDate);
        return ticket.getVisitors().size();
    }

    /** Removes the visitors of the ticket that are present; returns how many left. */
    public int exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.existing(attractionId);
        return attraction.exit(existingTicket(ticketId));
    }

    public Attraction attraction(String id) {
        return attractions.existing(id);
    }

    // ----- invoices

    /** Invoice of the directly purchased tickets of an existing user, built on demand. */
    public Invoice createUserInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        return new InvoiceDirector().construct(new UserInvoiceBuilder(user, tickets.directTicketsOf(user)));
    }

    /** Invoice of the group ticket purchases of an existing group, built on demand. */
    public Invoice createGroupInvoice(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            throw new IllegalArgumentException("No group with identifier " + groupId);
        }
        return new InvoiceDirector().construct(new GroupInvoiceBuilder(group, tickets.groupTicketsOf(group)));
    }

    private Ticket existingTicket(String id) {
        Ticket ticket = tickets.findAny(id);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket with identifier " + id);
        }
        return ticket;
    }
}
