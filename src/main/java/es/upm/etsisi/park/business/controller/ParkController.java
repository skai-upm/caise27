package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.EntryResult;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.PassType;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Central park management (single instance per execution). It is the entry point used by the
 * presentation layer and coordinates the rules that span several controllers, such as ticket-aware
 * user deletion, group locks and cascading ticket deletion. Collaborating controllers are injected.
 */
public final class ParkController {

    private static ParkController instance;

    private SystemController system;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private PassController passes;
    private AttractionController attractions;
    private InvoiceController invoices;

    private ParkController() {
    }

    public static synchronized ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    @SuppressWarnings("java:S107")
    public void initialize(SystemController systemController, UserController userController,
                           GroupController groupController, TicketController ticketController,
                           PassController passController, AttractionController attractionController,
                           InvoiceController invoiceController) {
        this.system = systemController;
        this.users = userController;
        this.groups = groupController;
        this.tickets = ticketController;
        this.passes = passController;
        this.attractions = attractionController;
        this.invoices = invoiceController;
    }

    // ----- System settings -----

    public LocalDate getCurrentDate() {
        return system.getCurrentDate();
    }

    public BigDecimal getCurrentPrice() {
        return system.getCurrentPrice();
    }

    public LocalDate setCurrentDate(String text) {
        return system.setCurrentDate(text);
    }

    public BigDecimal setCurrentPrice(String text) {
        return system.setCurrentPrice(text);
    }

    // ----- Users -----

    public UserController users() {
        return users;
    }

    public User createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        return users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String value) {
        users.update(email, password, field, value);
        tickets.persist();
    }

    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (tickets.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("User " + email + " has active tickets and cannot be deleted");
        }
        for (Group group : groups.groupsOf(user)) {
            if (tickets.isGroupLocked(group)) {
                throw new OperationNotAllowedException("User " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        groups.removeEverywhere(user);
        attractions.releaseEverywhere(user);
        users.remove(user);
        tickets.persist();
    }

    public boolean hasActiveTicket(User user) {
        return tickets.hasActiveTicket(user);
    }

    // ----- Groups -----

    public Group createGroup(String id, String name) {
        return groups.create(id, name);
    }

    public void addToGroup(String groupId, String email) {
        Group group = groups.require(groupId);
        User user = users.requireByEmail(email);
        requireUnlocked(group);
        groups.addMember(group, user);
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = groups.require(groupId);
        User user = users.requireByEmail(email);
        requireUnlocked(group);
        groups.removeMember(group, user);
    }

    public void deleteGroup(String groupId) {
        Group group = groups.require(groupId);
        requireUnlocked(group);
        tickets.detachGroup(group);
        groups.delete(group);
    }

    public Optional<Group> findGroup(String id) {
        return groups.find(id);
    }

    public boolean isGroupLocked(Group group) {
        return tickets.isGroupLocked(group);
    }

    private void requireUnlocked(Group group) {
        if (tickets.isGroupLocked(group)) {
            throw new OperationNotAllowedException("Group " + group.getId() + " is locked by an active group ticket");
        }
    }

    // ----- Tickets -----

    public IndividualTicket createTicket(String id, String email, String dateText) {
        return tickets.createIndividual(id, users.requireByEmail(email), dateText);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        return tickets.createGroupTicket(id, groups.require(groupId), dateText);
    }

    /**
     * Deletes a future individual ticket and all its passes, or nothing.
     */
    public void deleteTicket(String id) {
        IndividualTicket ticket = tickets.checkIndividualDeletion(id);
        passes.removeAllFor(List.of(ticket));
        tickets.remove(ticket);
    }

    /**
     * Deletes a future group ticket, the passes of its individual tickets and the individual tickets, or nothing.
     */
    public void deleteGroupTicket(String id) {
        GroupTicket ticket = tickets.checkGroupDeletion(id);
        passes.removeAllFor(ticket.getTickets());
        tickets.remove(ticket);
    }

    public Optional<Ticket> findTicket(String id) {
        return tickets.find(id);
    }

    public List<Ticket> getTickets() {
        return tickets.getTickets();
    }

    // ----- Passes -----

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) {
        return passes.buySpeedPasses(purchaseId, ticketId, quantity);
    }

    public SpecialPass buySpecialPass(String id, String ticketId) {
        return passes.buySpecialPass(id, ticketId);
    }

    public Pass refundSpeedPass(String id) {
        return passes.refund(id, PassType.SPEED);
    }

    public Pass refundSpecialPass(String id) {
        return passes.refund(id, PassType.SPECIAL);
    }

    public Optional<Pass> findPass(String id) {
        return passes.find(id);
    }

    // ----- Attractions -----

    public Attraction<?> createAttraction(String id, String name, String capacity, String type) {
        return attractions.create(id, name, capacity, type);
    }

    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) {
        return attractions.enter(attractionId, ticketId, speed);
    }

    public void exitAttraction(String attractionId, String email) {
        attractions.exit(attractionId, email);
    }

    public Attraction<?> requireAttraction(String id) {
        return attractions.require(id);
    }

    // ----- Invoices -----

    public Invoice createInvoice(String recipientType, String identifier) {
        return invoices.create(recipientType, identifier);
    }
}
