package es.upm.etsisi.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.model.attraction.Attraction;
import es.upm.etsisi.park.model.group.Group;
import es.upm.etsisi.park.model.ticket.GroupDiscount;
import es.upm.etsisi.park.model.ticket.GroupTicket;
import es.upm.etsisi.park.model.ticket.IndividualTicket;
import es.upm.etsisi.park.model.ticket.Money;
import es.upm.etsisi.park.model.ticket.Ticket;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.user.User;
import es.upm.etsisi.park.user.UserException;
import es.upm.etsisi.park.user.UserField;
import es.upm.etsisi.park.user.UserService;

/**
 * Central park management. There is exactly one instance per execution,
 * obtained with {@link #getInstance()}; {@link #open(ParkStorage)} loads its
 * state from a data directory. All operations that relate users, groups,
 * tickets and attractions go through this class.
 */
public final class Park {

    public static final BigDecimal DEFAULT_PRICE = Money.of(new BigDecimal("40"));

    private static final Park INSTANCE = new Park();
    private static final int DATE_LENGTH = 10;

    private ParkStorage storage;
    private UserService users;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final Map<String, Ticket> ticketIndex = new HashMap<>();
    private final List<Attraction> attractions = new ArrayList<>();
    private LocalDate systemDate;
    private BigDecimal basePrice;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /** Replaces the park state with the data stored in {@code storage}; date and price are reset. */
    public void open(ParkStorage storage) throws IOException {
        UserService loadedUsers = new UserService(storage.users());
        Map<String, User> usersByDni = new HashMap<>();
        for (User user : loadedUsers.getUsers()) {
            usersByDni.put(user.getDni(), user);
        }
        List<Group> loadedGroups = storage.groups().load(usersByDni);
        Map<String, Group> groupsById = new HashMap<>();
        for (Group group : loadedGroups) {
            groupsById.put(group.getId(), group);
        }
        List<Ticket> loadedTickets = storage.tickets().load(usersByDni, groupsById);
        List<Attraction> loadedAttractions = storage.attractions().load();

        this.storage = storage;
        this.users = loadedUsers;
        groups.clear();
        groups.addAll(loadedGroups);
        tickets.clear();
        ticketIndex.clear();
        for (Ticket ticket : loadedTickets) {
            register(ticket);
        }
        attractions.clear();
        attractions.addAll(loadedAttractions);
        systemDate = LocalDate.now();
        basePrice = DEFAULT_PRICE;
    }

    // ----------------------------------------------------------------- settings

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public LocalDate setSystemDate(String text) throws ParkException {
        systemDate = parseDate(text);
        return systemDate;
    }

    public BigDecimal setBasePrice(String text) throws ParkException {
        BigDecimal price = Money.parsePositive(text);
        if (price == null) {
            throw new ParkException("Invalid price '" + text + "': expected a positive amount with at most 2 decimals");
        }
        basePrice = price;
        return basePrice;
    }

    // -------------------------------------------------------------------- users

    public List<User> getUsers() {
        return users.getUsers();
    }

    public User findUser(String email) {
        return users.findByEmail(email);
    }

    public User createUser(String firstName, String surnames, String dni, String email, String password,
                           String phone, String card) throws ParkException {
        return users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, UserField field, String value) throws ParkException {
        users.update(email, password, field, value);
        saveParkData();
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group,
     * removing them from their groups. Historical tickets keep the user.
     */
    public void deleteUser(String email, String password) throws ParkException {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new UserException("User " + email + " has an active ticket and cannot be deleted");
        }
        List<Group> memberOf = groupsOf(user);
        for (Group group : memberOf) {
            if (isGroupLocked(group)) {
                throw new UserException("User " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        users.delete(email, password);
        for (Group group : memberOf) {
            group.removeMember(user);
        }
        saveParkData();
    }

    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(systemDate) && ticket.admits(user)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------- groups

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group findGroup(String id) {
        for (Group group : groups) {
            if (group.getId().equalsIgnoreCase(id)) {
                return group;
            }
        }
        return null;
    }

    public Group createGroup(String id, String name) throws ParkException {
        requireText(id, "Group identifier");
        requireText(name, "Group name");
        if (findGroup(id) != null) {
            throw new ParkException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        saveParkData();
        return group;
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        if (group.contains(user)) {
            throw new ParkException("User " + email + " already belongs to group " + group.getId());
        }
        requireUnlocked(group);
        group.addMember(user);
        saveParkData();
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        if (!group.contains(user)) {
            throw new ParkException("User " + email + " does not belong to group " + group.getId());
        }
        requireUnlocked(group);
        group.removeMember(user);
        saveParkData();
    }

    /** Deletes an unlocked group; its historical group tickets keep their data but lose the reference. */
    public void deleteGroup(String groupId) throws ParkException {
        Group group = requireGroup(groupId);
        requireUnlocked(group);
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.detachGroup();
            }
        }
        groups.remove(group);
        saveParkData();
    }

    /** A group is locked while any of its group tickets is dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && groupTicket.isActiveOn(systemDate)) {
                return true;
            }
        }
        return false;
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ tickets

    /** Top-level tickets: individual tickets and group tickets (member tickets are inside the latter). */
    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /** Any ticket by identifier, including member tickets of group tickets. */
    public Ticket findTicket(String id) {
        return ticketIndex.get(key(id));
    }

    /** Single check of the identifier space shared by every ticket type. */
    public boolean isTicketIdTaken(String id) {
        return ticketIndex.containsKey(key(id));
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws ParkException {
        requireFreeTicketId(id);
        User user = requireUser(email);
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, basePrice);
        register(ticket);
        saveParkData();
        return ticket;
    }

    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (ticket instanceof GroupTicket) {
            throw new ParkException("Ticket " + ticket.getId() + " is a group ticket: use GROUPTICKET DELETE");
        }
        GroupTicket container = containerOf(ticket);
        if (container != null) {
            throw new ParkException("Ticket " + ticket.getId() + " belongs to group ticket " + container.getId()
                    + " and cannot be deleted on its own");
        }
        requireDeletable(ticket);
        unregister(ticket);
        saveParkData();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        requireFreeTicketId(id);
        Group group = requireGroup(groupId);
        if (group.isEmpty()) {
            throw new ParkException("Group " + group.getId() + " has no members");
        }
        LocalDate date = requireUsableDate(dateText);
        List<User> members = group.getMembers();
        GroupDiscount discount = GroupDiscount.forMembers(members.size());
        List<IndividualTicket> memberTickets = new ArrayList<>();
        for (User member : members) {
            String memberId = GroupTicket.memberId(id, member.getDni());
            requireFreeTicketId(memberId);
            memberTickets.add(new IndividualTicket(memberId, date, member, basePrice, discount.getPercent()));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, memberTickets);
        register(ticket);
        saveParkData();
        return ticket;
    }

    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new ParkException("Ticket " + ticket.getId() + " is not a group ticket");
        }
        requireDeletable(ticket);
        unregister(ticket);
        saveParkData();
    }

    /** The group ticket whose composition contains {@code ticket}, found by traversing the tickets. */
    public GroupTicket containerOf(Ticket ticket) {
        for (Ticket candidate : tickets) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.getMembers().contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    // -------------------------------------------------------------- attractions

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction findAttraction(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equalsIgnoreCase(id)) {
                return attraction;
            }
        }
        return null;
    }

    public Attraction createAttraction(String id, String name, String capacityText) throws ParkException {
        requireText(id, "Attraction identifier");
        requireText(name, "Attraction name");
        int capacity = parseCapacity(capacityText);
        if (findAttraction(id) != null) {
            throw new ParkException("Attraction " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        saveParkData();
        return attraction;
    }

    /** Lets every holder of the ticket into the attraction, all or none. Returns how many entered. */
    public int enterAttraction(String attractionId, String ticketId) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.isValidOn(systemDate)) {
            throw new ParkException("Ticket " + ticket.getId() + " is valid on " + ticket.getDate()
                    + ", not on " + systemDate);
        }
        List<User> entering = ticket.getHolders();
        for (User user : entering) {
            Attraction current = attractionOf(user);
            if (current != null) {
                throw new ParkException("User " + user.getEmail() + " is already inside attraction " + current.getId());
            }
        }
        if (!attraction.enter(entering)) {
            throw new ParkException("Attraction " + attraction.getId() + " has " + attraction.getAvailable()
                    + " free place(s) but " + entering.size() + " user(s) need to enter");
        }
        return entering.size();
    }

    public User exitAttraction(String attractionId, String email) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        User user = attraction.findOccupantByEmail(email);
        if (user == null) {
            throw new ParkException("User " + email + " is not inside attraction " + attraction.getId());
        }
        attraction.exit(user);
        return user;
    }

    public Attraction requireAttraction(String id) throws ParkException {
        Attraction attraction = findAttraction(id);
        if (attraction == null) {
            throw new ParkException("Attraction " + id + " not found");
        }
        return attraction;
    }

    private Attraction attractionOf(User user) {
        for (Attraction attraction : attractions) {
            if (attraction.contains(user)) {
                return attraction;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ helpers

    public static LocalDate parseDate(String text) throws ParkException {
        if (text == null || text.length() != DATE_LENGTH) {
            throw new ParkException("Invalid date '" + text + "': expected yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date '" + text + "': expected yyyy-MM-dd");
        }
    }

    private static int parseCapacity(String text) throws ParkException {
        try {
            int capacity = Integer.parseInt(text);
            if (capacity > 0) {
                return capacity;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new ParkException("Invalid capacity '" + text + "': expected a positive integer");
    }

    private static void requireText(String value, String label) throws ParkException {
        if (value == null || value.trim().isEmpty()) {
            throw new ParkException(label + " must not be empty");
        }
    }

    private static String key(String id) {
        return id.toUpperCase();
    }

    private void register(Ticket ticket) {
        tickets.add(ticket);
        ticketIndex.put(key(ticket.getId()), ticket);
        if (ticket instanceof GroupTicket groupTicket) {
            for (IndividualTicket member : groupTicket.getMembers()) {
                ticketIndex.put(key(member.getId()), member);
            }
        }
    }

    private void unregister(Ticket ticket) {
        tickets.remove(ticket);
        ticketIndex.remove(key(ticket.getId()));
        if (ticket instanceof GroupTicket groupTicket) {
            for (IndividualTicket member : groupTicket.getMembers()) {
                ticketIndex.remove(key(member.getId()));
            }
        }
    }

    private void requireFreeTicketId(String id) throws ParkException {
        requireText(id, "Ticket identifier");
        if (isTicketIdTaken(id)) {
            throw new ParkException("Ticket identifier " + id + " is already in use");
        }
    }

    private LocalDate requireUsableDate(String text) throws ParkException {
        LocalDate date = parseDate(text);
        if (date.isBefore(systemDate)) {
            throw new ParkException("Date " + date + " is before the current date " + systemDate);
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) throws ParkException {
        if (!ticket.isDeletableOn(systemDate)) {
            throw new ParkException("Ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + "; only tickets after " + systemDate + " can be deleted");
        }
    }

    private Ticket requireTicket(String id) throws ParkException {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new ParkException("Ticket " + id + " not found");
        }
        return ticket;
    }

    private Group requireGroup(String id) throws ParkException {
        Group group = findGroup(id);
        if (group == null) {
            throw new ParkException("Group " + id + " not found");
        }
        return group;
    }

    private User requireUser(String email) throws ParkException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new ParkException("User with email " + email + " not found");
        }
        return user;
    }

    private void requireUnlocked(Group group) throws ParkException {
        if (isGroupLocked(group)) {
            throw new ParkException("Group " + group.getId() + " is locked by an active group ticket");
        }
    }

    /** Saves groups, tickets and attractions; users are saved by the user service. */
    private void saveParkData() throws ParkException {
        try {
            storage.groups().save(groups);
            storage.tickets().save(tickets, users.getUsers());
            storage.attractions().save(attractions);
        } catch (IOException e) {
            throw new ParkException("Could not save park data in " + storage.getDataDirectory() + ": "
                    + e.getMessage());
        }
    }
}
