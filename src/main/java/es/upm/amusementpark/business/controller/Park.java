package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassType;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Central park management: the single entry point to the park state. It
 * gives access to the controllers injected at startup, loads their data in
 * dependency order and offers the park operations used by the presentation
 * layer.
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

    /** Injects the controllers the park coordinates. */
    public void configure(SystemController system, UserController users, GroupController groups,
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

    /** Loads all stored data, resolving references in dependency order. */
    public void load() throws IOException {
        users.load();
        groups.load();
        attractions.load();
        tickets.load();
        passes.load();
    }

    public SystemController system() {
        return system;
    }

    public UserController users() {
        return users;
    }

    public GroupController groups() {
        return groups;
    }

    public TicketController tickets() {
        return tickets;
    }

    public PassController passes() {
        return passes;
    }

    public AttractionController attractions() {
        return attractions;
    }

    public InvoiceController invoices() {
        return invoices;
    }

    // ---------- System settings ----------

    public LocalDate getDate() {
        return system.getDate();
    }

    public BigDecimal getPrice() {
        return system.getPrice();
    }

    public void setDate(String text) throws ParkException {
        system.setDate(text);
    }

    public void setPrice(String text) throws ParkException {
        system.setPrice(text);
    }

    public static String formatAmount(BigDecimal amount) {
        return SystemController.formatAmount(amount);
    }

    // ---------- Users ----------

    public List<User> getUsers() {
        return users.getUsers();
    }

    public User findUserByEmail(String email) {
        return users.findByEmail(email);
    }

    public void createUser(String firstName, String surnames, String dni, String email, String password,
                           String phone, String card) throws ParkException {
        users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, UserField field, String value) throws ParkException {
        users.update(email, password, field, value);
    }

    public void deleteUser(String email, String password) throws ParkException {
        users.delete(email, password);
    }

    // ---------- Groups ----------

    public Group findGroup(String id) {
        return groups.find(id);
    }

    public List<Group> getGroups() {
        return groups.getGroups();
    }

    public boolean isLocked(Group group) {
        return groups.isLocked(group);
    }

    public void createGroup(String id, String name) throws ParkException {
        groups.create(id, name);
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        groups.addMember(groupId, email);
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        groups.removeMember(groupId, email);
    }

    public void deleteGroup(String groupId) throws ParkException {
        groups.delete(groupId);
    }

    // ---------- Tickets ----------

    public Ticket findTicket(String id) {
        return tickets.find(id);
    }

    public List<Ticket> getTickets() {
        return tickets.getTickets();
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws ParkException {
        return tickets.create(id, email, dateText);
    }

    public void deleteTicket(String id) throws ParkException {
        tickets.delete(id);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        return tickets.createGroupTicket(id, groupId, dateText);
    }

    public void deleteGroupTicket(String id) throws ParkException {
        tickets.deleteGroupTicket(id);
    }

    // ---------- Passes ----------

    public Pass findPass(String id) {
        return passes.find(id);
    }

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) throws ParkException {
        return passes.buySpeedPasses(purchaseId, ticketId, quantity);
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        return passes.buySpecialPass(id, ticketId);
    }

    public void refundSpeedPass(String id) throws ParkException {
        passes.refund(id, PassType.SPEED);
    }

    public void refundSpecialPass(String id) throws ParkException {
        passes.refund(id, PassType.SPECIAL);
    }

    // ---------- Invoices ----------

    public Invoice userInvoice(String email) throws NotFoundException {
        return invoices.userInvoice(email);
    }

    public Invoice groupInvoice(String groupId) throws NotFoundException {
        return invoices.groupInvoice(groupId);
    }

    // ---------- Attractions ----------

    public Attraction<?> findAttraction(String id) {
        return attractions.find(id);
    }

    public List<Attraction<?>> getAttractions() {
        return attractions.getAttractions();
    }

    public Attraction<?> requireAttraction(String id) throws NotFoundException {
        return attractions.require(id);
    }

    public void createAttraction(String id, String name, String capacityText) throws ParkException {
        attractions.create(id, name, capacityText, null);
    }

    public void createAttraction(String id, String name, String capacityText, String type) throws ParkException {
        attractions.create(id, name, capacityText, type);
    }

    public EntryResult enterAttraction(String attractionId, String ticketId) throws ParkException {
        return attractions.enter(attractionId, ticketId, null);
    }

    public EntryResult enterAttraction(String attractionId, String ticketId, String option) throws ParkException {
        return attractions.enter(attractionId, ticketId, option);
    }

    public void exitAttraction(String attractionId, String email) throws ParkException {
        attractions.exit(attractionId, email);
    }
}
