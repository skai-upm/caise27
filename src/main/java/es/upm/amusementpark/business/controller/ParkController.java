package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.repository.UserRepository;

/**
 * Main controller of the park. Holds the single system date and the base ticket
 * price, and applies the rules that involve several kinds of objects.
 */
public class ParkController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final UserController users;
    private final GroupController groups;
    private final TicketController tickets;
    private final AttractionController attractions;
    private LocalDate systemDate;
    private BigDecimal basePrice = DEFAULT_PRICE;

    public ParkController(UserRepository userRepository, GroupRepository groupRepository,
                          TicketRepository ticketRepository, AttractionRepository attractionRepository)
            throws IOException {
        this(userRepository, groupRepository, ticketRepository, attractionRepository, LocalDate.now());
    }

    public ParkController(UserRepository userRepository, GroupRepository groupRepository,
                          TicketRepository ticketRepository, AttractionRepository attractionRepository,
                          LocalDate startDate) throws IOException {
        this.users = new UserController(userRepository);
        this.groups = new GroupController(groupRepository, users::findByDni);
        this.tickets = new TicketController(ticketRepository, users::findByDni, groups::find);
        this.attractions = new AttractionController(attractionRepository);
        this.systemDate = startDate;
    }

    public UserController getUsers() {
        return users;
    }

    public GroupController getGroups() {
        return groups;
    }

    public TicketController getTickets() {
        return tickets;
    }

    public AttractionController getAttractions() {
        return attractions;
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    // --- system settings

    public void setSystemDate(String text) throws OperationException {
        systemDate = parseDate(text);
    }

    public void setBasePrice(String text) throws OperationException {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new OperationException("Invalid price: " + text);
        }
        if (price.signum() < 0) {
            throw new OperationException("Price must not be negative: " + text);
        }
        basePrice = price.setScale(2, RoundingMode.HALF_UP);
    }

    // --- users

    public void createUser(String name, String surname, String dni, String email,
                           String password, String phone, String card) throws OperationException {
        users.create(name, surname, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String value) throws OperationException {
        users.update(email, password, field, value);
    }

    public void deleteUser(String email, String password) throws OperationException {
        User user = users.authenticate(email, password);
        if (tickets.hasActiveTicket(user, systemDate)) {
            throw new OperationException("User " + email + " has tickets dated on or after " + systemDate);
        }
        for (Group g : groups.groupsOf(user)) {
            if (tickets.isGroupLocked(g, systemDate)) {
                throw new OperationException("User " + email + " belongs to locked group " + g.getId());
            }
        }
        users.delete(user);
        attractions.removeUserEverywhere(user);
        groups.removeUserEverywhere(user);
        tickets.save();
    }

    // --- groups

    public void createGroup(String id, String name) throws OperationException {
        groups.create(id, name);
    }

    public void addToGroup(String groupId, String email) throws OperationException {
        Group group = groups.require(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        groups.addMember(group, user);
    }

    public void removeFromGroup(String groupId, String email) throws OperationException {
        Group group = groups.require(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        groups.removeMember(group, user);
    }

    public void deleteGroup(String groupId) throws OperationException {
        Group group = groups.require(groupId);
        requireUnlocked(group);
        groups.delete(group);
    }

    // --- tickets

    public IndividualTicket createTicket(String id, String email, String dateText) throws OperationException {
        User user = requireUser(email);
        LocalDate date = requirePurchasableDate(dateText);
        return tickets.createIndividual(id, user, date, basePrice);
    }

    public void deleteTicket(String id) throws OperationException {
        tickets.deleteIndividual(id, systemDate);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws OperationException {
        Group group = groups.require(groupId);
        LocalDate date = requirePurchasableDate(dateText);
        return tickets.createGroup(id, group, date, basePrice);
    }

    public void deleteGroupTicket(String id) throws OperationException {
        tickets.deleteGroup(id, systemDate);
    }

    // --- attractions

    public void createAttraction(String id, String name, String capacityText) throws OperationException {
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            throw new OperationException("Capacity must be a positive integer: " + capacityText);
        }
        attractions.create(id, name, capacity);
    }

    public void enterAttraction(String attractionId, String ticketId) throws OperationException {
        Attraction attraction = attractions.require(attractionId);
        attraction.enter(requireTicket(ticketId), systemDate);
    }

    public int exitAttraction(String attractionId, String ticketId) throws OperationException {
        Attraction attraction = attractions.require(attractionId);
        return attraction.exit(requireTicket(ticketId));
    }

    public Attraction attractionStatus(String attractionId) throws OperationException {
        return attractions.require(attractionId);
    }

    // --- helpers

    private User requireUser(String email) throws OperationException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new OperationException("Unknown user: " + email);
        }
        return user;
    }

    private Ticket requireTicket(String id) throws OperationException {
        Ticket ticket = tickets.find(id);
        if (ticket == null) {
            throw new OperationException("Unknown ticket: " + id);
        }
        return ticket;
    }

    private void requireUnlocked(Group group) throws OperationException {
        if (tickets.isGroupLocked(group, systemDate)) {
            throw new OperationException("Group " + group.getId() + " is locked by a group ticket dated on or after "
                    + systemDate);
        }
    }

    private LocalDate requirePurchasableDate(String text) throws OperationException {
        LocalDate date = parseDate(text);
        if (date.isBefore(systemDate)) {
            throw new OperationException("Date " + date + " is before the current date " + systemDate);
        }
        return date;
    }

    public static LocalDate parseDate(String text) throws OperationException {
        if (text.length() != 10) {
            throw new OperationException("Invalid date (expected yyyy-MM-dd): " + text);
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new OperationException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }
}
