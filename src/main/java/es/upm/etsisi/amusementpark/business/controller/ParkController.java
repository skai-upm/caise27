package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ConflictException;
import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.model.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.model.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;
import es.upm.etsisi.amusementpark.business.repository.GroupRepository;
import es.upm.etsisi.amusementpark.business.repository.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single central manager of the park state: current system date and base price, groups, tickets
 * and attractions, and the rules that relate them to users and passes. Collaborating controllers
 * and repositories are injected with {@link #configure}.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;
    private UserController userController;
    private PassController passController;
    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AttractionRepository attractionRepository;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects collaborators and repositories and loads the stored park state. The user controller
     * must already be configured; the pass controller is loaded once tickets exist.
     */
    public void configure(LocalDate today, UserController users, PassController passes,
                          GroupRepository groupRepository, TicketRepository ticketRepository,
                          AttractionRepository attractionRepository) {
        this.currentDate = today;
        this.currentPrice = INITIAL_PRICE;
        this.userController = users;
        this.passController = passes;
        this.groupRepository = groupRepository;
        this.ticketRepository = ticketRepository;
        this.attractionRepository = attractionRepository;
        attractions.clear();
        attractions.addAll(attractionRepository.load());
        groups.clear();
        groups.addAll(groupRepository.load(users::findByDni));
        tickets.clear();
        tickets.addAll(ticketRepository.load(users::findByDni, this::findGroup));
        passes.load(this::findIndividualTicket);
    }

    private void saveAll() {
        userController.save();
        groupRepository.save(groups);
        ticketRepository.save(tickets);
        attractionRepository.save(attractions);
        passController.save();
    }

    // ----- system -----

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentDate(String text) throws ValidationException {
        currentDate = parseDate(text);
    }

    public void setCurrentPrice(String text) throws ValidationException {
        currentPrice = parsePrice(text);
    }

    static LocalDate parseDate(String text) throws ValidationException {
        try {
            return LocalDate.parse(text.strip());
        } catch (DateTimeParseException e) {
            throw new ValidationException("invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    static BigDecimal parsePrice(String text) throws ValidationException {
        String value = text.strip();
        int dot = value.indexOf('.');
        boolean valid = !value.isEmpty() && dot != 0 && dot != value.length() - 1
                && (dot < 0 || value.length() - dot - 1 <= 2);
        for (int i = 0; valid && i < value.length(); i++) {
            char c = value.charAt(i);
            valid = (c >= '0' && c <= '9') || (c == '.' && i == dot);
        }
        if (!valid) {
            throw new ValidationException("invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(value).setScale(2);
        if (price.signum() <= 0) {
            throw new ValidationException("price must be positive: " + text);
        }
        return price;
    }

    // ----- users -----

    public UserController getUserController() {
        return userController;
    }

    public void createUser(String firstName, String surnames, String dni, String email, String password,
                           String phone, String card) throws ParkException {
        userController.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String value) throws ParkException {
        userController.update(email, password, field, value);
        saveAll();
    }

    public void deleteUser(String email, String password) throws ParkException {
        User user = userController.authenticate(email, password);
        for (Ticket ticket : tickets) {
            if (ticket.isActive(currentDate) && ticket.getUsers().contains(user)) {
                throw new ForbiddenOperationException("user has an active ticket: " + ticket.getId());
            }
        }
        List<Group> memberships = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                if (isGroupLocked(group)) {
                    throw new ForbiddenOperationException("user belongs to locked group " + group.getId());
                }
                memberships.add(group);
            }
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        userController.remove(user);
        saveAll();
    }

    // ----- groups -----

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group findGroup(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public Group requireGroup(String id) throws NotFoundException {
        Group group = findGroup(id);
        if (group == null) {
            throw new NotFoundException("group not found: " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) throws ParkException {
        Group group = requireGroup(id);
        if (isGroupLocked(group)) {
            throw new ForbiddenOperationException("group " + id + " is locked by an active group ticket");
        }
        return group;
    }

    /**
     * A group is locked while any group ticket bought by it is dated today or later.
     */
    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group && gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public void createGroup(String id, String name) throws ParkException {
        requireIdentifier(id);
        if (findGroup(id) != null) {
            throw new ConflictException("group already exists: " + id);
        }
        groups.add(new Group(id, name));
        saveAll();
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        User user = userController.requireByEmail(email);
        if (!group.add(user)) {
            throw new ConflictException("user already in group: " + email);
        }
        saveAll();
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        User user = userController.requireByEmail(email);
        if (!group.remove(user)) {
            throw new NotFoundException("user is not in group: " + email);
        }
        saveAll();
    }

    public void deleteGroup(String groupId) throws ParkException {
        Group group = requireModifiableGroup(groupId);
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                gt.detachGroup();
            }
        }
        groups.remove(group);
        saveAll();
    }

    // ----- tickets -----

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    /**
     * Finds any ticket, including individual tickets inside group tickets.
     */
    public Ticket findTicket(String id) {
        for (Ticket ticket : tickets) {
            Ticket found = ticket.findTicket(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public IndividualTicket findIndividualTicket(String id) {
        return findTicket(id) instanceof IndividualTicket ticket ? ticket : null;
    }

    private IndividualTicket requireIndividualTicket(String id) throws NotFoundException {
        IndividualTicket ticket = findIndividualTicket(id);
        if (ticket == null) {
            throw new NotFoundException("individual ticket not found: " + id);
        }
        return ticket;
    }

    private void requireFreeTicketId(String id) throws ParkException {
        requireIdentifier(id);
        if (findTicket(id) != null) {
            throw new ConflictException("ticket identifier already in use: " + id);
        }
    }

    private LocalDate requireNotPast(String dateText) throws ParkException {
        LocalDate date = parseDate(dateText);
        if (date.isBefore(currentDate)) {
            throw new ForbiddenOperationException("ticket date is before the current date " + currentDate);
        }
        return date;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws ParkException {
        requireFreeTicketId(id);
        User user = userController.requireByEmail(email);
        LocalDate date = requireNotPast(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, currentPrice, 0, user);
        tickets.add(ticket);
        saveAll();
        return ticket;
    }

    /**
     * Deletes a future individual ticket together with all its passes.
     */
    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + id);
        }
        if (!tickets.contains(ticket) || !(ticket instanceof IndividualTicket individual)) {
            throw new ForbiddenOperationException("ticket " + id
                    + " is not a standalone individual ticket; use GROUPTICKET DELETE");
        }
        requireDeletable(ticket);
        passController.removePassesOf(List.of(individual));
        tickets.remove(ticket);
        saveAll();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        requireFreeTicketId(id);
        Group group = requireGroup(groupId);
        LocalDate date = requireNotPast(dateText);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ForbiddenOperationException("group " + groupId + " has no members");
        }
        for (User member : members) {
            String derived = GroupTicket.componentId(id, member);
            if (findTicket(derived) != null) {
                throw new ConflictException("ticket identifier already in use: " + derived);
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, currentPrice, group, members);
        tickets.add(ticket);
        saveAll();
        return ticket;
    }

    /**
     * Deletes a future group ticket, its composition and the passes of its composition tickets.
     */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = findTicket(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new NotFoundException("group ticket not found: " + id);
        }
        requireDeletable(ticket);
        passController.removePassesOf(groupTicket.getComposition());
        tickets.remove(ticket);
        saveAll();
    }

    private void requireDeletable(Ticket ticket) throws ForbiddenOperationException {
        if (!ticket.getDate().isAfter(currentDate)) {
            throw new ForbiddenOperationException("only tickets dated after " + currentDate + " can be deleted");
        }
    }

    // ----- passes -----

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, String quantity) throws ParkException {
        return passController.buySpeedPasses(purchaseId, requireIndividualTicket(ticketId), quantity, currentDate);
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws ParkException {
        return passController.buySpecialPass(id, requireIndividualTicket(ticketId), currentDate);
    }

    public void refundSpeedPass(String id) throws ParkException {
        passController.refund(id, SpeedPass.class, currentDate);
    }

    public void refundSpecialPass(String id) throws ParkException {
        passController.refund(id, SpecialPass.class, currentDate);
    }

    // ----- attractions -----

    public List<Attraction<?>> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction<?> findAttraction(String id) {
        for (Attraction<?> a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public Attraction<?> requireAttraction(String id) throws NotFoundException {
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            throw new NotFoundException("attraction not found: " + id);
        }
        return attraction;
    }

    public void createAttraction(String id, String name, String capacityText) throws ParkException {
        createAttraction(id, name, capacityText, AttractionType.GENERAL.name());
    }

    public void createAttraction(String id, String name, String capacityText, String typeText) throws ParkException {
        requireIdentifier(id);
        if (findAttraction(id) != null) {
            throw new ConflictException("attraction already exists: " + id);
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.strip());
        } catch (NumberFormatException e) {
            throw new ValidationException("invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new ValidationException("capacity must be positive: " + capacityText);
        }
        AttractionType type = AttractionType.parse(typeText.strip());
        attractions.add(type.create(id, name, capacity));
        saveAll();
    }

    /**
     * Admits the users of a ticket valid today; all of them or none, consuming the required passes.
     */
    public EntryResult enterAttraction(String attractionId, String ticketId, boolean speed) throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + ticketId);
        }
        EntryResult result = attraction.enter(ticket, speed, currentDate, passController);
        if (!result.consumedPasses().isEmpty()) {
            passController.save();
        }
        return result;
    }

    public void exitAttraction(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionId);
        User user = attraction.findOccupant(email);
        if (user == null) {
            throw new NotFoundException("user is not inside attraction " + attractionId + ": " + email);
        }
        attraction.exit(user);
    }

    private static void requireIdentifier(String id) throws ValidationException {
        if (id.isBlank()) {
            throw new ValidationException("identifier must not be empty");
        }
    }
}
