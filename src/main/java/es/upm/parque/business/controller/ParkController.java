package es.upm.parque.business.controller;

import es.upm.parque.business.model.Admission;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Main business controller. It owns the per-execution system variables (system date and current ticket price)
 * and applies the rules that involve several managers, such as the checks before deleting a user or modifying
 * a group. Failures are reported as {@link IllegalArgumentException} with a user-facing message.
 * There is a single instance, whose collaborators are injected once at startup with {@link #configure}.
 */
public class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private AttractionManager attractions;
    private InvoiceDirector invoiceDirector;
    private LocalDate currentDate;
    private BigDecimal currentPrice = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators (already configured) and starts a new execution: system date
     * {@code initialDate} and ticket price {@link #INITIAL_PRICE}.
     */
    public ParkController configure(UserManager users, GroupManager groups, TicketManager tickets,
            AttractionManager attractions, InvoiceDirector invoiceDirector, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoiceDirector = invoiceDirector;
        this.currentDate = initialDate;
        this.currentPrice = INITIAL_PRICE;
        return this;
    }

    /** The single system date that every time-dependent rule must use. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    /** The price applied to tickets sold from now on. */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** Sets the ticket price: a positive amount with at most two decimals. */
    public void setCurrentPrice(BigDecimal price) {
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new IllegalArgumentException(
                "Invalid price " + price.toPlainString() + ". Expected a positive amount with at most two decimals");
        }
        currentPrice = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    public User createUser(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        return users.create(name, surname, dni, email, password, phone, card);
    }

    /** Deletes a user without current or future tickets, removing it from groups and attractions. */
    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (tickets.hasTicketOnOrAfter(user, currentDate)) {
            throw new IllegalArgumentException("User " + email
                + " cannot be deleted: it has tickets dated on or after " + currentDate);
        }
        users.delete(email, password);
        groups.removeUserFromAll(user);
        tickets.detachOwner(user);
        attractions.removeUserEverywhere(user);
    }

    public void updateUser(String email, String password, UserField field, String value) {
        users.update(email, password, field, value);
    }

    public Group createGroup(String id, String name) {
        return groups.create(id, name);
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
        Group group = groups.find(groupId);
        groups.delete(groupId);
        tickets.detachGroup(group);
    }

    public IndividualTicket createTicket(String id, String email, LocalDate date) {
        return tickets.create(id, email, date, currentDate, currentPrice);
    }

    public void deleteTicket(String id) {
        tickets.delete(id, currentDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        return tickets.createGroupTicket(id, groupId, date, currentDate, currentPrice);
    }

    public void deleteGroupTicket(String id) {
        tickets.deleteGroupTicket(id, currentDate);
    }

    public Attraction createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public void deleteAttraction(String id) {
        attractions.delete(id);
    }

    /** @return the number of users who entered */
    public int enterAttraction(String attractionId, String ticketId) {
        Admission admission = requireAdmission(ticketId);
        attractions.enter(attractionId, admission, currentDate);
        return admission.getAdmittedUsers().size();
    }

    /** @return the number of users who left */
    public int exitAttraction(String attractionId, String ticketId) {
        return attractions.exit(attractionId, requireAdmission(ticketId));
    }

    public Attraction getAttraction(String id) {
        return attractions.require(id);
    }

    /** Builds the invoice of the registered user with that email, issued on the system date. */
    public Invoice createUserInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        return invoiceDirector.construct(new UserInvoiceBuilder(user, tickets.getTickets()), currentDate);
    }

    /** Builds the invoice of the group with that identifier, issued on the system date. */
    public Invoice createGroupInvoice(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            throw new IllegalArgumentException("No group with identifier " + groupId);
        }
        return invoiceDirector.construct(new GroupInvoiceBuilder(group, tickets.getTickets()), currentDate);
    }

    private void requireUnlocked(String groupId) {
        Group group = groups.find(groupId);
        if (group != null && tickets.isGroupLocked(group, currentDate)) {
            throw new IllegalArgumentException("Group " + groupId
                + " cannot be modified: it has group tickets dated on or after " + currentDate);
        }
    }

    private Admission requireAdmission(String ticketId) {
        Ticket ticket = tickets.findAdmission(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket with identifier " + ticketId);
        }
        return ticket;
    }
}
