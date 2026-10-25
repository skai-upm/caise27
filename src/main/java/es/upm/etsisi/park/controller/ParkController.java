package es.upm.etsisi.park.controller;

import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.service.UserException;
import es.upm.etsisi.park.service.UserService;
import es.upm.etsisi.park.validation.UserValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central park management. Exactly one instance exists per execution; it owns users, groups,
 * tickets, attractions and the configurable system date and base ticket price.
 */
public final class ParkController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static ParkController instance;

    private ParkStorage storage;
    private UserService users;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = DEFAULT_PRICE;

    private ParkController() {
    }

    public static synchronized ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /**
     * Loads the park state from the given storage and resets date and price to their start values.
     */
    public void configure(ParkStorage parkStorage) {
        this.storage = parkStorage;
        this.users = new UserService(parkStorage.users());
        groups.clear();
        tickets.clear();
        attractions.clear();
        currentDate = LocalDate.now();
        currentPrice = DEFAULT_PRICE;
        for (Group group : parkStorage.groups().loadAll(users::findByDni)) {
            groups.put(group.getId(), group);
        }
        for (Ticket ticket : parkStorage.tickets().loadAll(users::findByDni,
                id -> Optional.ofNullable(groups.get(id)))) {
            tickets.put(ticket.getId(), ticket);
        }
        for (Attraction attraction : parkStorage.attractions().loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
    }

    // ----- System settings -----

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public LocalDate setCurrentDate(String text) {
        currentDate = parseDate(text);
        return currentDate;
    }

    public BigDecimal setCurrentPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new UserException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new UserException("Invalid price: " + text + ". It must be a positive amount with at most 2 decimals");
        }
        currentPrice = price.setScale(2);
        return currentPrice;
    }

    // ----- Users -----

    public UserService users() {
        return users;
    }

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, String field, String value) {
        users.update(email, password, field, value);
        persist();
    }

    public void deleteUser(String email, String password) {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new UserException("User " + email + " has active tickets and cannot be deleted");
        }
        List<Group> memberships = groupsOf(user);
        for (Group group : memberships) {
            if (isGroupLocked(group)) {
                throw new UserException("User " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        for (Attraction attraction : attractions.values()) {
            attraction.release(user);
        }
        users.remove(user);
        persist();
    }

    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.isActiveOn(currentDate) && ticket.involves(user)) {
                return true;
            }
        }
        return false;
    }

    // ----- Groups -----

    public Group createGroup(String id, String name) {
        requireText(id, "Group identifier");
        requireText(name, "Group name");
        if (groups.containsKey(id)) {
            throw new UserException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        persist();
        return group;
    }

    public void addToGroup(String groupId, String email) {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        if (!group.add(user)) {
            throw new UserException("User " + email + " already belongs to group " + groupId);
        }
        persist();
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        if (!group.remove(user)) {
            throw new UserException("User " + email + " does not belong to group " + groupId);
        }
        persist();
    }

    public void deleteGroup(String groupId) {
        Group group = requireGroup(groupId);
        requireUnlocked(group);
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.detachGroup();
            }
        }
        groups.remove(groupId);
        persist();
    }

    public Optional<Group> findGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    // ----- Tickets -----

    public IndividualTicket createTicket(String id, String email, String dateText) {
        requireText(id, "Ticket identifier");
        User user = requireUser(email);
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, currentPrice, user);
        register(ticket);
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = requireTicket(id);
        if (ticket instanceof GroupTicket) {
            throw new UserException("Ticket " + id + " is a group ticket. Use GROUPTICKET DELETE");
        }
        if (!tickets.containsKey(id)) {
            throw new UserException("Ticket " + id + " belongs to a group ticket and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        tickets.remove(id);
        persist();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        requireText(id, "Ticket identifier");
        Group group = requireGroup(groupId);
        LocalDate date = requireUsableDate(dateText);
        if (group.isEmpty()) {
            throw new UserException("Group " + groupId + " has no members");
        }
        GroupTicket ticket = new GroupTicket(id, date, currentPrice, group, group.getMembers());
        register(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new UserException("Group ticket " + id + " does not exist");
        }
        requireDeletable(ticket);
        tickets.remove(id);
        persist();
    }

    public Optional<Ticket> findTicket(String id) {
        for (Ticket ticket : tickets.values()) {
            Optional<Ticket> found = ticket.findTicket(id);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    private void register(Ticket ticket) {
        Set<String> used = new HashSet<>();
        for (Ticket existing : tickets.values()) {
            used.addAll(existing.getIdentifiers());
        }
        for (String identifier : ticket.getIdentifiers()) {
            if (used.contains(identifier)) {
                throw new UserException("Ticket identifier " + identifier + " is already in use");
            }
        }
        tickets.put(ticket.getId(), ticket);
        persist();
    }

    private LocalDate requireUsableDate(String text) {
        LocalDate date = parseDate(text);
        if (date.isBefore(currentDate)) {
            throw new UserException("Date " + date + " is before the current system date " + currentDate);
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        if (!ticket.isDeletableOn(currentDate)) {
            throw new UserException("Ticket " + ticket.getId()
                    + " is for the current day or earlier and must be kept as a historical record");
        }
    }

    // ----- Attractions -----

    public Attraction createAttraction(String id, String name, String capacityText) {
        requireText(id, "Attraction identifier");
        requireText(name, "Attraction name");
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new UserException("Invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new UserException("Invalid capacity: " + capacityText + ". It must be a positive integer");
        }
        if (attractions.containsKey(id)) {
            throw new UserException("Attraction " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.put(id, attraction);
        persist();
        return attraction;
    }

    /**
     * Lets every holder of the ticket into the attraction, or nobody.
     *
     * @return the users who entered
     */
    public List<User> enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.isValidOn(currentDate)) {
            throw new UserException("Ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current date " + currentDate);
        }
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (attraction.isInside(holder)) {
                throw new UserException("User " + holder.getEmail() + " is already inside " + attractionId);
            }
        }
        if (!attraction.admitAll(holders)) {
            throw new UserException("Not enough capacity in " + attractionId + ": " + holders.size()
                    + " needed, " + attraction.getAvailablePlaces() + " available");
        }
        return holders;
    }

    public void exitAttraction(String attractionId, String email) {
        Attraction attraction = requireAttraction(attractionId);
        User user = attraction.findOccupant(email)
                .orElseThrow(() -> new UserException("User " + email + " is not inside " + attractionId));
        attraction.release(user);
    }

    public Attraction requireAttraction(String id) {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new UserException("Attraction " + id + " does not exist");
        }
        return attraction;
    }

    // ----- Helpers -----

    private void persist() {
        storage.groups().saveAll(groups.values());
        storage.tickets().saveAll(tickets.values());
        storage.attractions().saveAll(attractions.values());
    }

    private Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new UserException("Group " + id + " does not exist");
        }
        return group;
    }

    private void requireUnlocked(Group group) {
        if (isGroupLocked(group)) {
            throw new UserException("Group " + group.getId() + " is locked by an active group ticket");
        }
    }

    private User requireUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new UserException("User with email " + email + " does not exist"));
    }

    private Ticket requireTicket(String id) {
        return findTicket(id).orElseThrow(() -> new UserException("Ticket " + id + " does not exist"));
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new UserException("Invalid date: " + text + ". Expected format yyyy-MM-dd");
        }
    }

    private static void requireText(String value, String label) {
        if (!UserValidator.isNotBlank(value)) {
            throw new UserException(label + " must not be empty");
        }
    }
}
