package es.upm.parque.business.controller;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.EntryResult;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the park. It owns the single simulated current date and the individual ticket
 * price, and coordinates operations that involve several controllers.
 * Single instance (Singleton); its dependencies are injected with {@code configure} at startup.
 */
public class ParkController {

    private static final String SPEED_OPTION = "SPEED";
    private static final int SPEEDPASS_PACK = 10;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final ParkController INSTANCE = new ParkController();

    private UserController userController;
    private GroupController groupController;
    private TicketController ticketController;
    private AttractionController attractionController;
    private InvoiceController invoiceController;
    private LocalDate currentDate;
    private BigDecimal ticketPrice;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborating controllers and starts a new run: the current date is the operating system
     * date and the ticket price is 40.00.
     */
    public void configure(UserController userController, GroupController groupController,
                          TicketController ticketController, AttractionController attractionController,
                          InvoiceController invoiceController) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.invoiceController = invoiceController;
        this.currentDate = LocalDate.now();
        this.ticketPrice = new BigDecimal("40.00");
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

    public InvoiceController getInvoiceController() {
        return invoiceController;
    }

    public AttractionController getAttractionController() {
        return attractionController;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---- SYSTEM ----

    public String setDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + text;
        }
        currentDate = date;
        return "OK SYSTEM DATE -> " + currentDate;
    }

    public String setPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return "ERROR invalid price: " + text;
        }
        if (price.signum() <= 0) {
            return "ERROR price must be positive";
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return "OK SYSTEM PRICE -> " + formatPrice(ticketPrice);
    }

    // ---- USER ----

    public String createUser(String name, String surname, String dni, String email, String password,
                             String phone, String card) throws IOException {
        return userController.create(name, surname, dni, email, password, phone, card);
    }

    public String updateUser(String email, String password, String field, String value) throws IOException {
        return userController.update(email, password, field, value);
    }

    public String deleteUser(String email, String password) throws IOException {
        User user = userController.authenticate(email, password);
        if (user == null) {
            return "ERROR invalid email or password";
        }
        if (ticketController.hasActiveTicket(user, currentDate)) {
            return "ERROR user has tickets dated on or after " + currentDate;
        }
        groupController.removeUserFromAll(user);
        return userController.delete(email, password);
    }

    // ---- GROUP ----

    public String createGroup(String id, String name) throws IOException {
        return groupController.create(id, name);
    }

    public String addToGroup(String groupId, String email) throws IOException {
        Group group = groupController.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        if (ticketController.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupController.add(group, user);
    }

    public String removeFromGroup(String groupId, String email) throws IOException {
        Group group = groupController.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        if (ticketController.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupController.remove(group, user);
    }

    public String deleteGroup(String groupId) throws IOException {
        Group group = groupController.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        if (ticketController.isGroupLocked(group, currentDate)) {
            return lockedMessage(group);
        }
        return groupController.delete(group);
    }

    private String lockedMessage(Group group) {
        return "ERROR group " + group.getId() + " has group tickets dated on or after " + currentDate;
    }

    // ---- TICKET ----

    public String createTicket(String id, String email, String dateText) throws IOException {
        if (id.isBlank()) {
            return "ERROR ticket id is required";
        }
        if (ticketController.find(id) != null) {
            return "ERROR ticket " + id + " already exists";
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return "ERROR user " + email + " does not exist";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + dateText;
        }
        if (date.isBefore(currentDate)) {
            return "ERROR ticket date is before the current date " + currentDate;
        }
        IndividualTicket ticket = new IndividualTicket(id, date, user, ticketPrice);
        ticketController.add(ticket);
        return "OK TICKET CREATED -> " + formatPrice(ticket.getPrice());
    }

    public String deleteTicket(String id) throws IOException {
        Ticket ticket = ticketController.find(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            return "ERROR ticket " + id + " does not exist";
        }
        if (individual.getGroupTicket() != null) {
            return "ERROR ticket " + id + " belongs to group ticket " + individual.getGroupTicket().getId();
        }
        if (!ticket.isDeletableOn(currentDate)) {
            return "ERROR only tickets dated after " + currentDate + " can be deleted";
        }
        ticketController.remove(ticket);
        return "OK TICKET DELETED";
    }

    public String createGroupTicket(String id, String groupId, String dateText) throws IOException {
        if (id.isBlank()) {
            return "ERROR ticket id is required";
        }
        if (ticketController.find(id) != null) {
            return "ERROR ticket " + id + " already exists";
        }
        Group group = groupController.find(groupId);
        if (group == null) {
            return "ERROR group " + groupId + " does not exist";
        }
        List<User> members = group.getUsers();
        if (members.isEmpty()) {
            return "ERROR group " + groupId + " has no members";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "ERROR invalid date (expected yyyy-MM-dd): " + dateText;
        }
        if (date.isBefore(currentDate)) {
            return "ERROR ticket date is before the current date " + currentDate;
        }
        List<String> internalIds = new ArrayList<>();
        for (int i = 1; i <= members.size(); i++) {
            String internalId = id + "-" + i;
            if (ticketController.find(internalId) != null) {
                return "ERROR ticket " + internalId + " already exists";
            }
            internalIds.add(internalId);
        }
        GroupTicket groupTicket = new GroupTicket(id, date, group, GroupTicket.totalPrice(ticketPrice, members.size()));
        for (int i = 0; i < members.size(); i++) {
            groupTicket.addTicket(new IndividualTicket(internalIds.get(i), date, members.get(i), ticketPrice));
        }
        ticketController.add(groupTicket);
        return "OK GROUP TICKET CREATED -> " + formatPrice(groupTicket.getPrice());
    }

    public String deleteGroupTicket(String id) throws IOException {
        Ticket ticket = ticketController.find(id);
        if (!(ticket instanceof GroupTicket)) {
            return "ERROR group ticket " + id + " does not exist";
        }
        if (!ticket.isDeletableOn(currentDate)) {
            return "ERROR only group tickets dated after " + currentDate + " can be deleted";
        }
        ticketController.remove(ticket);
        return "OK GROUP TICKET DELETED";
    }

    // ---- INVOICE ----

    public String createInvoice(String recipientType, String key) {
        String type = recipientType.trim().toUpperCase();
        if ("USER".equals(type)) {
            User user = userController.findByEmail(key);
            if (user == null) {
                return "ERROR user " + key + " does not exist";
            }
            return invoiceController.createUserInvoice(user, currentDate).toString();
        }
        if ("GROUP".equals(type)) {
            Group group = groupController.find(key);
            if (group == null) {
                return "ERROR group " + key + " does not exist";
            }
            return invoiceController.createGroupInvoice(group, currentDate).toString();
        }
        return "ERROR invalid invoice recipient type: " + recipientType + " (USER or GROUP)";
    }

    // ---- PASSES ----

    private IndividualTicket passTicket(String ticketId) {
        Ticket ticket = ticketController.find(ticketId);
        return ticket instanceof IndividualTicket individual ? individual : null;
    }

    public String createSpeedPasses(String ticketId, String quantityText) throws IOException {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText.trim());
        } catch (NumberFormatException e) {
            quantity = 0;
        }
        if (quantity != 1 && quantity != SPEEDPASS_PACK) {
            return "ERROR SpeedPasses can only be bought in quantities of 1 or " + SPEEDPASS_PACK;
        }
        IndividualTicket ticket = passTicket(ticketId);
        if (ticket == null) {
            return "ERROR individual ticket " + ticketId + " does not exist";
        }
        if (ticket.getDate().isBefore(currentDate)) {
            return "ERROR ticket " + ticketId + " is in the past";
        }
        int next = 1;
        for (SpeedPass p : ticket.getSpeedPasses()) {
            String n = p.getId().substring(p.getId().lastIndexOf("-SP") + 3);
            next = Math.max(next, Integer.parseInt(n) + 1);
        }
        BigDecimal unit = SpeedPass.priceFor(ticket);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String id = ticketId + "-SP" + (next + i);
            ticket.addSpeedPass(new SpeedPass(id, unit, false));
            ids.add(id);
        }
        ticketController.savePasses();
        BigDecimal total = unit.multiply(BigDecimal.valueOf(quantity));
        String range = quantity == 1 ? ids.get(0) : ids.get(0) + " .. " + ids.get(ids.size() - 1);
        return "OK " + quantity + " SPEEDPASS CREATED (" + range + ") -> " + formatPrice(total);
    }

    public String createSpecialPass(String ticketId) throws IOException {
        IndividualTicket ticket = passTicket(ticketId);
        if (ticket == null) {
            return "ERROR individual ticket " + ticketId + " does not exist";
        }
        if (ticket.getDate().isBefore(currentDate)) {
            return "ERROR ticket " + ticketId + " is in the past";
        }
        if (ticket.getSpecialPass() != null) {
            return "ERROR ticket " + ticketId + " already has a Special Pass";
        }
        SpecialPass pass = new SpecialPass(ticketId + "-SPECIAL", SpecialPass.priceFor(ticket), false);
        ticket.setSpecialPass(pass);
        ticketController.savePasses();
        return "OK SPECIALPASS CREATED (" + pass.getId() + ") -> " + formatPrice(pass.getPrice());
    }

    public String deleteSpeedPass(String passId) throws IOException {
        Pass pass = ticketController.findPass(passId);
        if (!(pass instanceof SpeedPass speedPass)) {
            return "ERROR SpeedPass " + passId + " does not exist";
        }
        IndividualTicket ticket = ticketController.ownerOf(pass);
        String error = refundError(pass, ticket);
        if (error != null) {
            return error;
        }
        ticket.removeSpeedPass(speedPass);
        ticketController.savePasses();
        return "OK SPEEDPASS REFUNDED -> " + formatPrice(pass.getPrice());
    }

    public String deleteSpecialPass(String passId) throws IOException {
        Pass pass = ticketController.findPass(passId);
        if (!(pass instanceof SpecialPass)) {
            return "ERROR Special Pass " + passId + " does not exist";
        }
        IndividualTicket ticket = ticketController.ownerOf(pass);
        String error = refundError(pass, ticket);
        if (error != null) {
            return error;
        }
        ticket.setSpecialPass(null);
        ticketController.savePasses();
        return "OK SPECIALPASS REFUNDED -> " + formatPrice(pass.getPrice());
    }

    private String refundError(Pass pass, IndividualTicket ticket) {
        if (pass.isConsumed()) {
            return "ERROR pass " + pass.getId() + " has been used and cannot be refunded";
        }
        if (!ticket.isDeletableOn(currentDate)) {
            return "ERROR only passes of tickets dated after " + currentDate + " can be refunded";
        }
        return null;
    }

    // ---- ATTRACTION ----

    public String createAttraction(String id, String name, String capacity, String type) throws IOException {
        return attractionController.create(id, name, capacity, type);
    }

    public String deleteAttraction(String id) throws IOException {
        return attractionController.delete(id);
    }

    /**
     * Enters an attraction with a ticket; option may be null or SPEED.
     */
    public String enterAttraction(String attractionId, String ticketId, String option) throws IOException {
        boolean speed = false;
        if (option != null) {
            if (!SPEED_OPTION.equalsIgnoreCase(option.trim())) {
                return "ERROR invalid option: " + option + " (only SPEED)";
            }
            speed = true;
        }
        Attraction<?> attraction = attractionController.find(attractionId);
        if (attraction == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        Ticket ticket = ticketController.find(ticketId);
        if (ticket == null) {
            return "ERROR ticket " + ticketId + " does not exist";
        }
        EntryResult result = attraction.enter(ticket, currentDate, speed);
        if (!result.isAdmitted()) {
            return "ERROR " + result.error();
        }
        StringBuilder reply = new StringBuilder("OK ATTRACTION ENTERED (" + result.users() + " users)");
        if (!result.consumedPasses().isEmpty()) {
            ticketController.savePasses();
            List<String> ids = new ArrayList<>();
            for (Pass p : result.consumedPasses()) {
                ids.add(p.getId());
            }
            reply.append(" - ").append(result.consumedPasses().get(0).getKind()).append(" consumed: ")
                    .append(String.join(", ", ids));
        }
        return reply.toString();
    }

    public String exitAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = attractionController.find(attractionId);
        if (attraction == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        Ticket ticket = ticketController.find(ticketId);
        if (ticket == null) {
            return "ERROR ticket " + ticketId + " does not exist";
        }
        int left = attraction.exit(ticket);
        if (left == 0) {
            return "ERROR no user of ticket " + ticketId + " is in attraction " + attractionId;
        }
        return "OK ATTRACTION EXITED (" + left + " users)";
    }

    public String attractionStatus(String attractionId) {
        Attraction<?> a = attractionController.find(attractionId);
        if (a == null) {
            return "ERROR attraction " + attractionId + " does not exist";
        }
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();
        sb.append("ATTRACTION ").append(a.getId()).append(nl)
                .append("  Name: ").append(a.getName()).append(nl)
                .append("  Type: ").append(a.getType()).append(nl)
                .append("  Capacity: ").append(a.getCapacity()).append(nl)
                .append("  Present: ").append(a.getUsers().size()).append(nl)
                .append("  Available: ").append(a.getAvailable()).append(nl)
                .append("  Users:");
        if (a.getUsers().isEmpty()) {
            sb.append(" (none)");
        }
        for (User u : a.getUsers()) {
            sb.append(nl).append("    - ").append(u.getName()).append(' ').append(u.getSurname())
                    .append(" (").append(u.getEmail()).append(')');
        }
        return sb.toString();
    }
}
