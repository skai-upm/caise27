package com.amusementpark.business.controller;

import com.amusementpark.business.model.Attraction;
import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.Ticket;
import com.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Main controller: owns the single system date and the base ticket price, and coordinates the
 * managers for rules that span users, groups, tickets and attractions.
 */
public class ParkController {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final ParkController INSTANCE = new ParkController();

    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private AttractionController attractions;
    private InvoiceController invoices;

    private LocalDate systemDate;
    private BigDecimal basePrice;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborating controllers (already configured) and starts a new execution on the
     * given date with the default base price.
     */
    public void configure(UserController users, GroupController groups, TicketController tickets,
                          AttractionController attractions, InvoiceController invoices, LocalDate startDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoices = invoices;
        this.systemDate = startDate;
        this.basePrice = DEFAULT_PRICE;
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    // --- system settings -------------------------------------------------------------------

    public String setSystemDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return "ERROR: Invalid date (expected yyyy-MM-dd): " + text;
        }
        systemDate = date;
        return "OK: System date set to " + systemDate;
    }

    public String setBasePrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            price = null;
        }
        if (price == null || price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            return "ERROR: Invalid price (expected a positive amount with up to two decimals): " + text;
        }
        basePrice = price.setScale(2);
        return "OK: Base price set to " + basePrice.toPlainString();
    }

    // --- users -----------------------------------------------------------------------------

    public String createUser(String name, String surname, String dni, String email,
                             String password, String phone, String card) {
        return users.create(name, surname, dni, email, password, phone, card);
    }

    public String updateUser(String email, String password, String field, String value) {
        String result = users.update(email, password, field, value);
        if (result.startsWith("OK") && !tickets.persist()) {
            return result + " (warning: tickets file could not be refreshed)";
        }
        return result;
    }

    public String deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (user == null) {
            return users.delete(email, password);
        }
        if (tickets.hasActiveTicket(user, systemDate)) {
            return "ERROR: " + email + " has tickets dated on or after " + systemDate + " and cannot be deleted.";
        }
        List<Group> memberships = groups.removeFromAll(user);
        if (memberships == null) {
            return "ERROR: Could not save groups file.";
        }
        String result = users.delete(email, password);
        if (!result.startsWith("OK")) {
            groups.restore(user, memberships);
        }
        return result;
    }

    // --- groups ----------------------------------------------------------------------------

    public String createGroup(String id, String name) {
        return groups.create(id, name);
    }

    public String addToGroup(String groupId, String email) {
        Group group = groups.find(groupId);
        String problem = checkGroupChangeable(group, groupId);
        if (problem != null) {
            return problem;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR: No user with email " + email + ".";
        }
        return groups.addMember(group, user);
    }

    public String removeFromGroup(String groupId, String email) {
        Group group = groups.find(groupId);
        String problem = checkGroupChangeable(group, groupId);
        if (problem != null) {
            return problem;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR: No user with email " + email + ".";
        }
        return groups.removeMember(group, user);
    }

    public String deleteGroup(String groupId) {
        Group group = groups.find(groupId);
        String problem = checkGroupChangeable(group, groupId);
        return problem != null ? problem : groups.delete(group);
    }

    private String checkGroupChangeable(Group group, String groupId) {
        if (group == null) {
            return "ERROR: Group " + groupId + " does not exist.";
        }
        if (tickets.isGroupLocked(group, systemDate)) {
            return "ERROR: Group " + groupId + " has group tickets dated on or after " + systemDate
                    + " and cannot be changed.";
        }
        return null;
    }

    // --- tickets ---------------------------------------------------------------------------

    public String createTicket(String ticketId, String email, String dateText) {
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR: No user with email " + email + ".";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR: Invalid date (expected yyyy-MM-dd): " + dateText;
        }
        return tickets.createTicket(ticketId, user, date, basePrice, systemDate);
    }

    public String deleteTicket(String ticketId) {
        return tickets.deleteTicket(ticketId, systemDate);
    }

    public String createGroupTicket(String ticketId, String groupId, String dateText) {
        Group group = groups.find(groupId);
        if (group == null) {
            return "ERROR: Group " + groupId + " does not exist.";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR: Invalid date (expected yyyy-MM-dd): " + dateText;
        }
        return tickets.createGroupTicket(ticketId, group, date, basePrice, systemDate);
    }

    public String deleteGroupTicket(String ticketId) {
        return tickets.deleteGroupTicket(ticketId, systemDate);
    }

    // --- attractions -----------------------------------------------------------------------

    public String createAttraction(String id, String name, String capacity) {
        return attractions.create(id, name, capacity);
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR: Attraction " + attractionId + " does not exist.";
        }
        Ticket ticket = tickets.find(ticketId);
        if (ticket == null) {
            return "ERROR: Ticket " + ticketId + " does not exist.";
        }
        String problem = attraction.enter(ticket, systemDate);
        if (problem != null) {
            return "ERROR: " + problem;
        }
        return "OK: " + ticket.getHolders().size() + " user(s) entered " + attractionId;
    }

    /** Exit by ticket identifier or, when the value contains '@', by user email. No date check. */
    public String exitAttraction(String attractionId, String ticketOrEmail) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR: Attraction " + attractionId + " does not exist.";
        }
        List<User> leaving;
        if (ticketOrEmail.contains("@")) {
            User user = users.findByEmail(ticketOrEmail);
            leaving = user == null ? List.of() : List.of(user);
        } else {
            Ticket ticket = tickets.find(ticketOrEmail);
            if (ticket == null) {
                return "ERROR: Ticket " + ticketOrEmail + " does not exist.";
            }
            leaving = ticket.getHolders();
        }
        int left = attraction.exit(leaving);
        if (left == 0) {
            return "ERROR: Nobody identified by " + ticketOrEmail + " is in " + attractionId + ".";
        }
        return "OK: " + left + " user(s) left " + attractionId;
    }

    public String attractionStatus(String attractionId) {
        Attraction attraction = attractions.find(attractionId);
        if (attraction == null) {
            return "ERROR: Attraction " + attractionId + " does not exist.";
        }
        return AttractionController.status(attraction);
    }

    // --- invoices --------------------------------------------------------------------------

    /** {@code INVOICE CREATE;USER;<email>} or {@code INVOICE CREATE;GROUP;<groupId>}. */
    public String createInvoice(String recipientType, String recipient) {
        switch (recipientType.trim().toUpperCase()) {
            case "USER" -> {
                User user = users.findByEmail(recipient);
                return user == null ? "ERROR: No user with email " + recipient + "."
                        : invoices.userInvoice(user, systemDate).toText();
            }
            case "GROUP" -> {
                Group group = groups.find(recipient);
                return group == null ? "ERROR: Group " + recipient + " does not exist."
                        : invoices.groupInvoice(group, systemDate).toText();
            }
            default -> {
                return "ERROR: Invoice recipient type must be USER or GROUP: " + recipientType;
            }
        }
    }

    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
