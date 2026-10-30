package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Main controller of the park. Holds the single system date and base ticket price used by every
 * business rule, and coordinates the rules that span users, groups, tickets and attractions.
 */
public class ParkController {

    private static ParkController instance;

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final String DOES_NOT_EXIST = " does not exist";

    private LocalDate systemDate;
    private BigDecimal basePrice;
    private UserController userController;
    private GroupController groupController;
    private TicketController ticketController;
    private AttractionController attractionController;
    private InvoiceController invoiceController;
    private PassController passController;

    private ParkController() {
    }

    /** Returns the unique instance of the main controller. */
    public static synchronized ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /**
     * Injects the collaborating controllers and starts a new execution: system date = today, base price = 40.00.
     */
    public void configure(UserController userController, GroupController groupController,
            TicketController ticketController, AttractionController attractionController,
            InvoiceController invoiceController, PassController passController) {
        this.systemDate = LocalDate.now(ZoneId.systemDefault());
        this.basePrice = DEFAULT_PRICE;
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.invoiceController = invoiceController;
        this.passController = passController;
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public UserController getUserController() {
        return userController;
    }

    public GroupController getGroupController() {
        return groupController;
    }

    public TicketController getTicketController() {
        return ticketController;
    }

    public AttractionController getAttractionController() {
        return attractionController;
    }

    // System settings

    public String setSystemDate(String text) {
        LocalDate date = Formats.parseDate(text);
        if (date == null) {
            return invalidDate(text);
        }
        systemDate = date;
        return "OK: system date set to " + date;
    }

    public String setBasePrice(String text) {
        BigDecimal price = parsePrice(text.trim());
        if (price == null) {
            return "ERROR: invalid price " + text + " (expected a positive amount such as 60.00)";
        }
        basePrice = price;
        return "OK: base price set to " + Formats.formatPrice(price);
    }

    private static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        if (text.isEmpty() || dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1) {
            return null;
        }
        if (dot >= 0 && text.length() - dot - 1 > 2) {
            return null;
        }
        for (char c : text.toCharArray()) {
            if (c != '.' && (c < '0' || c > '9')) {
                return null;
            }
        }
        BigDecimal price = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        return price.signum() > 0 ? price : null;
    }

    // Users

    public String deleteUser(String email, String password) {
        User user = userController.authenticate(email, password);
        if (user == null) {
            return "ERROR: invalid email or password";
        }
        if (ticketController.hasActiveTicket(user, systemDate)) {
            return "ERROR: user " + email + " has tickets dated on or after " + systemDate + " and cannot be deleted";
        }
        String result = userController.delete(email, password);
        if (result.startsWith("OK")) {
            groupController.removeUserFromAll(user);
        }
        return result;
    }

    // Groups

    public String createGroup(String id, String name) {
        return groupController.create(id, name);
    }

    public String addToGroup(String groupId, String email) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        if (ticketController.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupController.addMember(group, user);
    }

    public String removeFromGroup(String groupId, String email) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        if (ticketController.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupController.removeMember(group, user);
    }

    public String deleteGroup(String groupId) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        if (ticketController.isGroupLocked(group, systemDate)) {
            return lockedGroup(groupId);
        }
        return groupController.delete(group);
    }

    // Tickets

    public String createTicket(String id, String email, String dateText) {
        User user = userController.findByEmail(email);
        if (user == null) {
            return unknownUser(email);
        }
        LocalDate date = Formats.parseDate(dateText);
        if (date == null) {
            return invalidDate(dateText);
        }
        return ticketController.createIndividual(id, user, date, basePrice, systemDate);
    }

    public String deleteTicket(String id) {
        return ticketController.deleteIndividual(id, systemDate);
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            return unknownGroup(groupId);
        }
        LocalDate date = Formats.parseDate(dateText);
        if (date == null) {
            return invalidDate(dateText);
        }
        return ticketController.createGroup(id, group, date, basePrice, systemDate);
    }

    public String deleteGroupTicket(String id) {
        return ticketController.deleteGroup(id, systemDate);
    }

    // Attractions

    public String createAttraction(String id, String name, String capacity, String type) {
        return attractionController.create(id, name, capacity, type);
    }

    public String enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionController.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        Ticket ticket = ticketController.findById(ticketId);
        if (ticket == null) {
            return unknownTicket(ticketId);
        }
        return attractionController.enter(attraction, ticket, systemDate);
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractionController.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        Ticket ticket = ticketController.findById(ticketId);
        if (ticket == null) {
            return unknownTicket(ticketId);
        }
        return attractionController.exit(attraction, ticket);
    }

    public String attractionStatus(String attractionId) {
        Attraction attraction = attractionController.findById(attractionId);
        if (attraction == null) {
            return unknownAttraction(attractionId);
        }
        return attractionController.status(attraction);
    }

    // Passes

    public String buySpeedPasses(String ticketId, String quantity) {
        return passController.buySpeedPasses(ticketId, quantity, systemDate);
    }

    public String buySpecialPass(String ticketId) {
        return passController.buySpecialPass(ticketId, systemDate);
    }

    public String refundSpeedPass(String passId) {
        return passController.refundSpeedPass(passId, systemDate);
    }

    public String refundSpecialPass(String passId) {
        return passController.refundSpecialPass(passId, systemDate);
    }

    // Invoices

    public String createInvoice(String recipientType, String identifier) {
        switch (recipientType.trim()) {
            case "USER": {
                User user = userController.findByEmail(identifier);
                return user == null ? unknownUser(identifier) : invoiceController.userInvoice(user).render();
            }
            case "GROUP": {
                Group group = groupController.findById(identifier);
                return group == null ? unknownGroup(identifier) : invoiceController.groupInvoice(group).render();
            }
            default:
                return "ERROR: invoice type must be USER or GROUP";
        }
    }

    private static String invalidDate(String text) {
        return "ERROR: invalid date " + text + " (expected yyyy-MM-dd)";
    }

    private static String unknownUser(String email) {
        return "ERROR: user " + email + DOES_NOT_EXIST;
    }

    private static String unknownGroup(String id) {
        return "ERROR: group " + id + DOES_NOT_EXIST;
    }

    private static String lockedGroup(String id) {
        return "ERROR: group " + id + " has current or future group tickets and cannot be modified";
    }

    private static String unknownTicket(String id) {
        return "ERROR: ticket " + id + DOES_NOT_EXIST;
    }

    private static String unknownAttraction(String id) {
        return "ERROR: attraction " + id + DOES_NOT_EXIST;
    }
}
