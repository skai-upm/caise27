package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.attraction.EntryResult;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;
import es.upm.amusementpark.business.model.ticket.GroupTicket;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.model.ticket.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Central park management: the single entry point through which the presentation layer reaches users,
 * groups, tickets, passes, attractions and invoices. It delegates to the injected controllers and
 * persists the park state after every operation that changes it.
 */
public final class Park {

    private static final Park INSTANCE = new Park();

    private SystemController system;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private PassController passes;
    private AttractionController attractions;
    private InvoiceController invoices;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    public void setControllers(SystemController system, UserController users, GroupController groups,
                               TicketController tickets, PassController passes, AttractionController attractions,
                               InvoiceController invoices) {
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
        this.attractions = attractions;
        this.invoices = invoices;
    }

    /**
     * Loads every element from the injected repositories, in dependency order.
     */
    public void load() throws PersistenceException {
        users.load();
        groups.load();
        attractions.load();
        tickets.load();
        passes.load();
    }

    private void saveAll() throws PersistenceException {
        users.save();
        groups.save();
        attractions.save();
        tickets.save();
        passes.save();
    }

    // ---- System date and price -------------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return system.getCurrentDate();
    }

    public BigDecimal getCurrentPrice() {
        return system.getCurrentPrice();
    }

    public void setCurrentDate(String value) throws ParkException {
        system.setCurrentDate(value);
    }

    public void setCurrentPrice(String value) throws ParkException {
        system.setCurrentPrice(value);
    }

    // ---- Users -----------------------------------------------------------------------------------

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) throws ParkException {
        users.create(firstName, surnames, dni, email, password, phone, card);
        saveAll();
    }

    public void updateUser(String email, String password, UserField field, String value) throws ParkException {
        users.update(email, password, field, value);
        saveAll();
    }

    public void deleteUser(String email, String password) throws ParkException {
        users.delete(email, password);
        saveAll();
    }

    public User findUser(String email) {
        return users.findByEmail(email);
    }

    public List<User> getUsers() {
        return users.getUsers();
    }

    public boolean isActiveUser(User user) {
        return users.isActive(user);
    }

    // ---- Groups ----------------------------------------------------------------------------------

    public void createGroup(String id, String name) throws ParkException {
        groups.create(id, name);
        saveAll();
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        groups.addMember(groupId, email);
        saveAll();
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        groups.removeMember(groupId, email);
        saveAll();
    }

    public void deleteGroup(String groupId) throws ParkException {
        groups.delete(groupId);
        saveAll();
    }

    public boolean isLocked(Group group) {
        return groups.isLocked(group);
    }

    public Group findGroup(String id) {
        return groups.findGroup(id);
    }

    // ---- Tickets ---------------------------------------------------------------------------------

    public IndividualTicket createTicket(String id, String email, String date) throws ParkException {
        IndividualTicket ticket = tickets.createTicket(id, email, date);
        saveAll();
        return ticket;
    }

    public void deleteTicket(String id) throws ParkException {
        tickets.deleteTicket(id);
        saveAll();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) throws ParkException {
        GroupTicket ticket = tickets.createGroupTicket(id, groupId, date);
        saveAll();
        return ticket;
    }

    public void deleteGroupTicket(String id) throws ParkException {
        tickets.deleteGroupTicket(id);
        saveAll();
    }

    public Ticket findTicket(String id) {
        return tickets.findTicket(id);
    }

    public List<Ticket> getTickets() {
        return tickets.getTickets();
    }

    public List<IndividualTicket> ticketsOf(User user) {
        return tickets.ticketsOf(user);
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        return tickets.groupTicketsOf(group);
    }

    // ---- Passes ----------------------------------------------------------------------------------

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) throws ParkException {
        List<SpeedPass> created = passes.buySpeedPasses(purchaseId, ticketId, quantity);
        saveAll();
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        SpecialPass pass = passes.buySpecialPass(id, ticketId);
        saveAll();
        return pass;
    }

    public void refundSpeedPass(String id) throws ParkException {
        passes.refundSpeedPass(id);
        saveAll();
    }

    public void refundSpecialPass(String id) throws ParkException {
        passes.refundSpecialPass(id);
        saveAll();
    }

    public Pass findPass(String id) {
        return passes.findPass(id);
    }

    public List<Pass> getPasses() {
        return passes.getPasses();
    }

    // ---- Attractions -----------------------------------------------------------------------------

    public void createAttraction(String id, String name, String capacity) throws ParkException {
        createAttraction(id, name, capacity, null);
    }

    public void createAttraction(String id, String name, String capacity, String type) throws ParkException {
        attractions.create(id, name, capacity, type);
        saveAll();
    }

    /**
     * Normal entry (no SpeedPass); returns the number of users who entered.
     */
    public int enterAttraction(String attractionId, String ticketId) throws ParkException {
        return enterAttraction(attractionId, ticketId, false).admitted();
    }

    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) throws ParkException {
        EntryResult result = attractions.enter(attractionId, ticketId, speed);
        saveAll();
        return result;
    }

    public void exitAttraction(String attractionId, String email) throws ParkException {
        attractions.exit(attractionId, email);
    }

    public Attraction<?> findAttraction(String id) throws NotFoundException {
        return attractions.requireAttraction(id);
    }

    // ---- Invoices --------------------------------------------------------------------------------

    public Invoice userInvoice(String email) throws ParkException {
        return invoices.userInvoice(email);
    }

    public Invoice groupInvoice(String groupId) throws ParkException {
        return invoices.groupInvoice(groupId);
    }
}
