package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.ticket.GroupTicket;
import es.upm.amusementpark.model.ticket.IndividualTicket;
import es.upm.amusementpark.model.ticket.Ticket;
import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.validation.ValueParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management. There is exactly one instance per execution ({@link #getInstance()}); it owns
 * users, groups, tickets and attractions, the current system date and the current base ticket price, and
 * enforces every rule that relates these elements.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final Park INSTANCE = new Park();

    private final UserService users = new UserService();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;
    private ParkStorage storage;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Resets the park to the start-of-execution state: date {@code today}, price 40.00, and the elements
     * stored in {@code storage}, which is also used for every later save.
     */
    public void initialize(ParkStorage storage, LocalDate today) throws IOException {
        this.storage = storage;
        this.currentDate = today;
        this.currentPrice = INITIAL_PRICE;
        groups.clear();
        tickets.clear();
        attractions.clear();

        users.load(storage.users().load());
        Map<String, User> usersByDni = new HashMap<>();
        for (User user : users.getUsers()) {
            usersByDni.put(user.getDni(), user);
        }
        for (Group group : storage.groups().load(usersByDni)) {
            groups.put(group.getId(), group);
        }
        for (Attraction attraction : storage.attractions().load()) {
            attractions.put(attraction.getId(), attraction);
        }
        for (Ticket ticket : storage.tickets().load(usersByDni, groups)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    // ---- System date and price -------------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentDate(String value) throws OperationException {
        currentDate = ValueParser.parseDate(value);
    }

    public void setCurrentPrice(String value) throws OperationException {
        currentPrice = ValueParser.parsePrice(value);
    }

    // ---- Users -----------------------------------------------------------------------------------

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) throws OperationException {
        users.create(firstName, surnames, dni, email, password, phone, card);
        persist();
    }

    public void updateUser(String email, String password, UserField field, String value)
            throws OperationException {
        users.update(email, password, field, value);
        persist();
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group, removing them from their
     * groups and from any attraction. Historical tickets keep referring to the deleted user.
     */
    public void deleteUser(String email, String password) throws OperationException {
        User user = users.authenticate(email, password);
        for (IndividualTicket ticket : ticketsOf(user)) {
            if (ticket.isActive(currentDate)) {
                throw new OperationException("user has an active ticket: " + ticket.getId());
            }
        }
        List<Group> memberships = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.hasMember(user)) {
                if (isLocked(group)) {
                    throw new OperationException("user belongs to locked group " + group.getId());
                }
                memberships.add(group);
            }
        }
        for (Group group : memberships) {
            group.removeMember(user);
        }
        for (Attraction attraction : attractions.values()) {
            attraction.exit(user);
        }
        users.remove(user);
        persist();
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

    public void createGroup(String id, String name) throws OperationException {
        requireIdentifier(id, "group");
        if (groups.containsKey(id)) {
            throw new OperationException("group already exists: " + id);
        }
        groups.put(id, new Group(id, name));
        persist();
    }

    public void addToGroup(String groupId, String email) throws OperationException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        if (group.hasMember(user)) {
            throw new OperationException("user already belongs to group " + groupId);
        }
        group.addMember(user);
        persist();
    }

    public void removeFromGroup(String groupId, String email) throws OperationException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireUnlocked(group);
        if (!group.removeMember(user)) {
            throw new OperationException("user does not belong to group " + groupId);
        }
        persist();
    }

    /**
     * Deletes a group without active group tickets; its historical group tickets lose the reference.
     */
    public void deleteGroup(String groupId) throws OperationException {
        Group group = requireGroup(groupId);
        if (isLocked(group)) {
            throw new OperationException("group has an active group ticket: " + groupId);
        }
        for (GroupTicket ticket : groupTicketsOf(group)) {
            ticket.detachGroup();
        }
        groups.remove(groupId);
        persist();
    }

    /**
     * A group is locked while any of its group tickets is active; this is derived, never stored.
     */
    public boolean isLocked(Group group) {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    // ---- Tickets ---------------------------------------------------------------------------------

    public IndividualTicket createTicket(String id, String email, String date) throws OperationException {
        requireIdentifier(id, "ticket");
        requireFreeTicketId(id);
        User user = requireUser(email);
        LocalDate day = requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, currentPrice, 0);
        tickets.put(id, ticket);
        persist();
        return ticket;
    }

    public void deleteTicket(String id) throws OperationException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new OperationException("not an individual ticket, use GROUPTICKET DELETE: " + id);
        }
        if (containingGroupTicket(individual) != null) {
            throw new OperationException("ticket belongs to a group ticket: " + id);
        }
        requireDeletable(ticket);
        tickets.remove(id);
        persist();
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) throws OperationException {
        requireIdentifier(id, "ticket");
        requireFreeTicketId(id);
        Group group = requireGroup(groupId);
        LocalDate day = requireNotPast(date);
        if (group.isEmpty()) {
            throw new OperationException("group has no members: " + groupId);
        }
        List<User> members = group.getMembers();
        for (User member : members) {
            requireFreeTicketId(id + ":" + member.getDni());
        }
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(id + ":" + member.getDni(), day, member, currentPrice, discount));
        }
        GroupTicket groupTicket = new GroupTicket(id, day, group, composition);
        tickets.put(id, groupTicket);
        for (IndividualTicket member : composition) {
            tickets.put(member.getId(), member);
        }
        persist();
        return groupTicket;
    }

    public void deleteGroupTicket(String id) throws OperationException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new OperationException("not a group ticket, use TICKET DELETE: " + id);
        }
        requireDeletable(ticket);
        for (IndividualTicket member : groupTicket.getComposition()) {
            tickets.remove(member.getId());
        }
        tickets.remove(id);
        persist();
    }

    public Ticket findTicket(String id) {
        return tickets.get(id);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    /**
     * Individual tickets (standalone or in a composition) of a user, found by traversing all tickets.
     */
    public List<IndividualTicket> ticketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }

    /**
     * Group tickets bought by a group, found by traversing all tickets.
     */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    // ---- Attractions -----------------------------------------------------------------------------

    public void createAttraction(String id, String name, String capacity) throws OperationException {
        requireIdentifier(id, "attraction");
        int places = ValueParser.parsePositiveInt(capacity);
        if (attractions.containsKey(id)) {
            throw new OperationException("attraction already exists: " + id);
        }
        attractions.put(id, new Attraction(id, name, places));
        persist();
    }

    /**
     * Admits everyone covered by a ticket valid today, all or none.
     *
     * @return the number of users who entered
     */
    public int enterAttraction(String attractionId, String ticketId) throws OperationException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.isValidOn(currentDate)) {
            throw new OperationException("ticket is not valid today: " + ticketId);
        }
        List<User> entering = ticket.getUsers();
        for (User user : entering) {
            if (!users.isActive(user)) {
                throw new OperationException("user is no longer active: " + user.getEmail());
            }
            if (attraction.contains(user)) {
                throw new OperationException("user already inside the attraction: " + user.getEmail());
            }
        }
        if (!attraction.enter(entering)) {
            throw new OperationException("not enough capacity: " + attraction.getFreePlaces()
                    + " place(s) available, " + entering.size() + " needed");
        }
        return entering.size();
    }

    public void exitAttraction(String attractionId, String email) throws OperationException {
        Attraction attraction = requireAttraction(attractionId);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.exit(occupant);
                return;
            }
        }
        throw new OperationException("user is not inside the attraction: " + email);
    }

    public Attraction findAttraction(String id) throws OperationException {
        return requireAttraction(id);
    }

    // ---- Helpers ---------------------------------------------------------------------------------

    private void persist() throws OperationException {
        try {
            storage.users().saveAll(users.getUsers());
            storage.groups().saveAll(groups.values());
            storage.attractions().saveAll(attractions.values());
            storage.tickets().saveAll(tickets.values(), users::isActive);
        } catch (IOException e) {
            throw new OperationException("could not save data: " + e.getMessage());
        }
    }

    private GroupTicket containingGroupTicket(IndividualTicket ticket) {
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    private static void requireIdentifier(String id, String kind) throws OperationException {
        if (id.isEmpty()) {
            throw new OperationException(kind + " identifier must not be empty");
        }
    }

    private void requireFreeTicketId(String id) throws OperationException {
        if (tickets.containsKey(id)) {
            throw new OperationException("ticket identifier already exists: " + id);
        }
    }

    private LocalDate requireNotPast(String date) throws OperationException {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(currentDate)) {
            throw new OperationException("date is in the past: " + date);
        }
        return day;
    }

    private void requireDeletable(Ticket ticket) throws OperationException {
        if (!ticket.isDeletable(currentDate)) {
            throw new OperationException("only tickets dated after today can be deleted: " + ticket.getId());
        }
    }

    private void requireUnlocked(Group group) throws OperationException {
        if (isLocked(group)) {
            throw new OperationException("group has an active group ticket: " + group.getId());
        }
    }

    private User requireUser(String email) throws OperationException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new OperationException("user not found: " + email);
        }
        return user;
    }

    private Group requireGroup(String id) throws OperationException {
        Group group = groups.get(id);
        if (group == null) {
            throw new OperationException("group not found: " + id);
        }
        return group;
    }

    private Ticket requireTicket(String id) throws OperationException {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new OperationException("ticket not found: " + id);
        }
        return ticket;
    }

    private Attraction requireAttraction(String id) throws OperationException {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new OperationException("attraction not found: " + id);
        }
        return attraction;
    }
}
