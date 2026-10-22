package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.ParkStore;
import es.upm.amusementpark.persistence.ParkStore.ParkData;
import es.upm.amusementpark.persistence.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management: the single object that owns the park state and
 * coordinates operations between users, groups, tickets and attractions.
 */
public final class Park {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final Park INSTANCE = new Park();

    private ParkStore store;
    private UserService users;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private LocalDate today;
    private BigDecimal price;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Loads all park state from {@code store}, discarding any previous state,
     * and resets the system date and price.
     */
    public void open(ParkStore store, LocalDate today) throws IOException {
        this.store = store;
        this.today = today;
        this.price = DEFAULT_PRICE;
        this.groups.clear();
        this.tickets.clear();
        this.attractions.clear();
        this.users = new UserService(new StoreUserRepository());
        for (Group group : store.loadGroups(users::findByDni)) {
            groups.put(group.getId(), group);
        }
        for (Attraction attraction : store.loadAttractions()) {
            attractions.put(attraction.getId(), attraction);
        }
        for (Ticket ticket : store.loadTickets(users::findByDni, groups::get)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    public LocalDate getDate() {
        return today;
    }

    public BigDecimal getPrice() {
        return price;
    }

    // ---------- System settings ----------

    public void setDate(String text) throws ParkException {
        today = parseDate(text);
    }

    public void setPrice(String text) throws ParkException {
        price = parsePrice(text);
    }

    /** Parses a strict ISO {@code yyyy-MM-dd} date. */
    public static LocalDate parseDate(String text) throws ParkException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date '" + text + "' (expected an existing date as yyyy-MM-dd)");
        }
    }

    /** Parses a positive amount with at most two decimals, such as {@code 45.50}. */
    public static BigDecimal parsePrice(String text) throws ParkException {
        if (!isPlainAmount(text)) {
            throw new ParkException("Invalid price '" + text + "' (expected a positive amount such as 45.50)");
        }
        BigDecimal value = new BigDecimal(text);
        if (value.signum() <= 0) {
            throw new ParkException("Invalid price '" + text + "' (the price must be greater than zero)");
        }
        return value.setScale(2);
    }

    private static boolean isPlainAmount(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int dot = text.indexOf('.');
        if (dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1) {
            return false;
        }
        if (dot > 0 && text.length() - dot - 1 > 2) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '.' && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---------- Users ----------

    public List<User> getUsers() {
        return users.getUsers();
    }

    public User findUserByEmail(String email) {
        return users.findByEmail(email);
    }

    public void createUser(String firstName, String surnames, String dni, String email, String password,
                           String phone, String card) throws UserException {
        users.create(firstName, surnames, dni, email, password, phone, card);
    }

    public void updateUser(String email, String password, UserField field, String value) throws UserException {
        users.update(email, password, field, value);
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group,
     * removing them from their groups and from any attraction. Historical tickets keep referencing the
     * deleted user as a fiscal record.
     */
    public void deleteUser(String email, String password) throws ParkException {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new ParkException("User " + email + " has active tickets (dated " + today
                    + " or later) and cannot be deleted");
        }
        for (Group group : groups.values()) {
            if (group.contains(user) && isLocked(group)) {
                throw new ParkException("User " + email + " belongs to group " + group.getId()
                        + ", which has an active group ticket");
            }
        }
        Map<Group, Integer> memberships = new LinkedHashMap<>();
        for (Group group : groups.values()) {
            int position = group.remove(user);
            if (position >= 0) {
                memberships.put(group, position);
            }
        }
        Attraction inside = locate(user);
        int seat = inside == null ? -1 : inside.exit(user);
        try {
            users.delete(email, password);
        } catch (UserException e) {
            memberships.forEach((group, position) -> group.add(position, user));
            if (inside != null) {
                inside.restore(seat, user);
            }
            throw e;
        }
    }

    // ---------- Groups ----------

    public Group findGroup(String id) {
        return groups.get(id);
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public void createGroup(String id, String name) throws ParkException {
        requireText(id, "Group identifier");
        requireText(name, "Group name");
        if (groups.containsKey(id)) {
            throw new ParkException("Group " + id + " already exists");
        }
        groups.put(id, new Group(id, name));
        persist(() -> groups.remove(id));
    }

    public void addToGroup(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireModifiable(group);
        if (!group.add(user)) {
            throw new ParkException(email + " is already a member of group " + groupId);
        }
        persist(() -> group.remove(user));
    }

    public void removeFromGroup(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = requireUser(email);
        requireModifiable(group);
        int position = group.remove(user);
        if (position < 0) {
            throw new ParkException(email + " is not a member of group " + groupId);
        }
        persist(() -> group.add(position, user));
    }

    /**
     * Deletes an unlocked group. Its historical group tickets are kept but no
     * longer reference it.
     */
    public void deleteGroup(String groupId) throws ParkException {
        Group group = requireGroup(groupId);
        requireModifiable(group);
        List<GroupTicket> purchases = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                purchases.add(groupTicket);
            }
        }
        groups.remove(groupId);
        purchases.forEach(GroupTicket::detachGroup);
        persist(() -> {
            groups.put(groupId, group);
            purchases.forEach(ticket -> ticket.restoreGroup(group));
        });
    }

    /**
     * A group is locked while it has an active group ticket. The state is
     * derived from the tickets and the current date, never stored.
     */
    public boolean isLocked(Group group) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && groupTicket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    private void requireModifiable(Group group) throws ParkException {
        if (isLocked(group)) {
            throw new ParkException("Group " + group.getId()
                    + " has an active group ticket and cannot be modified");
        }
    }

    // ---------- Tickets ----------

    /**
     * Finds a ticket of any kind by identifier, including the individual
     * tickets that compose group tickets.
     */
    public Ticket findTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getComposition()) {
                    if (member.getId().equals(id)) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    /** Single uniqueness check shared by every ticket type. */
    public boolean isTicketIdTaken(String id) {
        return findTicket(id) != null;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) throws ParkException {
        requireText(id, "Ticket identifier");
        requireFreeTicketId(id);
        User user = requireUser(email);
        LocalDate date = requireUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, price);
        tickets.put(id, ticket);
        persist(() -> tickets.remove(id));
        return ticket;
    }

    /** Deletes a standalone individual ticket dated after the current date. */
    public void deleteTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (ticket instanceof GroupTicket) {
            throw new ParkException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        GroupTicket container = containerOf(ticket);
        if (container != null) {
            throw new ParkException("Ticket " + id + " belongs to group ticket " + container.getId()
                    + " and can only be deleted with it");
        }
        requireDeletable(ticket);
        tickets.remove(id);
        persist(() -> tickets.put(id, ticket));
    }

    /** Deletes a group ticket dated after the current date, with its individual tickets. */
    public void deleteGroupTicket(String id) throws ParkException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new ParkException("Ticket " + id + " is not a group ticket");
        }
        requireDeletable(ticket);
        tickets.remove(id);
        persist(() -> tickets.put(id, ticket));
    }

    private Ticket requireTicket(String id) throws ParkException {
        Ticket ticket = findTicket(id);
        if (ticket == null) {
            throw new ParkException("No ticket with identifier " + id);
        }
        return ticket;
    }

    private GroupTicket containerOf(Ticket member) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getComposition().contains(member)) {
                return groupTicket;
            }
        }
        return null;
    }

    /**
     * Buys a group ticket for the current members of a non-empty group. The
     * identifiers of every individual ticket are checked before anything is
     * created.
     */
    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        requireText(id, "Ticket identifier");
        requireFreeTicketId(id);
        Group group = requireGroup(groupId);
        if (group.isEmpty()) {
            throw new ParkException("Group " + groupId + " has no members");
        }
        LocalDate date = requireUsableDate(dateText);
        GroupTicket ticket = GroupTicket.purchase(id, date, group, price);
        for (IndividualTicket member : ticket.getComposition()) {
            requireFreeTicketId(member.getId());
        }
        tickets.put(id, ticket);
        persist(() -> tickets.remove(id));
        return ticket;
    }

    /** Whether {@code user} holds any ticket dated today or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.isActiveOn(today) && ticket.isHeldBy(user)) {
                return true;
            }
        }
        return false;
    }

    private void requireFreeTicketId(String id) throws ParkException {
        if (isTicketIdTaken(id)) {
            throw new ParkException("A ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate requireUsableDate(String dateText) throws ParkException {
        LocalDate date = parseDate(dateText);
        if (date.isBefore(today)) {
            throw new ParkException("Tickets cannot be created for past dates (current date " + today + ")");
        }
        return date;
    }

    private void requireDeletable(Ticket ticket) throws ParkException {
        if (!ticket.isDeletableOn(today)) {
            throw new ParkException("Ticket " + ticket.getId() + " is dated " + ticket.getDate()
                    + " and is kept as a historical record; only tickets after " + today + " can be deleted");
        }
    }

    // ---------- Attractions ----------

    public Attraction findAttraction(String id) {
        return attractions.get(id);
    }

    public List<Attraction> getAttractions() {
        return new ArrayList<>(attractions.values());
    }

    public void createAttraction(String id, String name, String capacityText) throws ParkException {
        requireText(id, "Attraction identifier");
        requireText(name, "Attraction name");
        int capacity = parseCapacity(capacityText);
        if (attractions.containsKey(id)) {
            throw new ParkException("Attraction " + id + " already exists");
        }
        attractions.put(id, new Attraction(id, name, capacity));
        persist(() -> attractions.remove(id));
    }

    public Attraction requireAttraction(String id) throws ParkException {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new ParkException("No attraction with identifier " + id);
        }
        return attraction;
    }

    /**
     * Lets every holder of a ticket valid today enter the attraction together,
     * or nobody if any of them cannot.
     *
     * @return the number of users who entered
     */
    public int enterAttraction(String attractionId, String ticketId) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.isValidOn(today)) {
            throw new ParkException("Ticket " + ticketId + " is valid only on " + ticket.getDate()
                    + ", not on " + today);
        }
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (users.findByEmail(holder.getEmail()) != holder) {
                throw new ParkException("Ticket " + ticketId + " belongs to " + holder.getEmail()
                        + ", who is no longer a registered user");
            }
            Attraction current = locate(holder);
            if (current != null) {
                throw new ParkException(holder.getEmail() + " is already inside attraction " + current.getId());
            }
        }
        if (!attraction.hasRoomFor(holders.size())) {
            throw new ParkException("Attraction " + attractionId + " has " + attraction.getFreePlaces()
                    + " free places, " + holders.size() + " needed; nobody entered");
        }
        attraction.enter(holders);
        return holders.size();
    }

    public void exitAttraction(String attractionId, String email) throws ParkException {
        Attraction attraction = requireAttraction(attractionId);
        User occupant = attraction.findOccupant(email);
        if (occupant == null) {
            throw new ParkException(email + " is not inside attraction " + attractionId);
        }
        attraction.exit(occupant);
    }

    /** The attraction {@code user} is currently inside, found by traversing attractions. */
    private Attraction locate(User user) {
        for (Attraction attraction : attractions.values()) {
            if (attraction.contains(user)) {
                return attraction;
            }
        }
        return null;
    }

    private static int parseCapacity(String text) throws ParkException {
        boolean digits = text != null && !text.isEmpty() && text.length() <= 9;
        for (int i = 0; digits && i < text.length(); i++) {
            digits = text.charAt(i) >= '0' && text.charAt(i) <= '9';
        }
        int capacity = digits ? Integer.parseInt(text) : 0;
        if (capacity <= 0) {
            throw new ParkException("Invalid capacity '" + text + "' (expected a positive whole number)");
        }
        return capacity;
    }

    // ---------- Helpers ----------

    private Group requireGroup(String id) throws ParkException {
        Group group = groups.get(id);
        if (group == null) {
            throw new ParkException("No group with identifier " + id);
        }
        return group;
    }

    private User requireUser(String email) throws ParkException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        return user;
    }

    private static void requireText(String value, String label) throws ParkException {
        if (value == null || value.trim().isEmpty()) {
            throw new ParkException(label + " must not be empty");
        }
    }

    /**
     * Saves the whole park state. If saving fails, {@code undo} restores the
     * in-memory state as it was before the command.
     */
    private void persist(Runnable undo) throws ParkException {
        try {
            store.saveAll(data(users.getUsers()));
        } catch (IOException e) {
            undo.run();
            throw new ParkException("Could not save park data: " + e.getMessage(), e);
        }
    }

    private ParkData data(Collection<User> allUsers) {
        return new ParkData(allUsers, groups.values(), tickets.values(), attractions.values());
    }

    /** Lets {@link UserService} persist user changes together with the rest of the park state. */
    private final class StoreUserRepository implements UserRepository {

        @Override
        public List<User> loadAll() throws IOException {
            return store.loadUsers();
        }

        @Override
        public void saveAll(Collection<User> allUsers) throws IOException {
            store.saveAll(data(allUsers));
        }
    }
}
