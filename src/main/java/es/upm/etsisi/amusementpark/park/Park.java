package es.upm.etsisi.amusementpark.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.OperationException;
import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.persistence.AttractionStorage;
import es.upm.etsisi.amusementpark.persistence.GroupStorage;
import es.upm.etsisi.amusementpark.persistence.TicketStorage;
import es.upm.etsisi.amusementpark.persistence.UserStorage;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.ticket.Ticket;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserField;
import es.upm.etsisi.amusementpark.user.UserManager;

/**
 * The single central park manager: owns users, groups, tickets and attractions,
 * the current system date and base price, and enforces the rules that relate them.
 * Lock states are always derived from the tickets and the current date.
 */
public final class Park {
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final Park INSTANCE = new Park();

    private LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
    private BigDecimal basePrice = DEFAULT_PRICE;
    private UserManager users;
    private GroupStorage groupStorage;
    private TicketStorage ticketStorage;
    private AttractionStorage attractionStorage;
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Attraction> attractions = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Replaces the whole park state with the data stored at these paths and resets
     * the system date and price to their start-of-execution values.
     */
    public void load(DataPaths paths) throws IOException {
        UserStorage userStorage = new UserStorage(paths.users());
        UserManager loadedUsers = new UserManager(userStorage);
        GroupStorage loadedGroupStorage = new GroupStorage(paths.groups());
        TicketStorage loadedTicketStorage = new TicketStorage(paths.tickets());
        AttractionStorage loadedAttractionStorage = new AttractionStorage(paths.attractions());

        Map<String, User> usersByDni = new HashMap<>();
        loadedUsers.getUsers().forEach(u -> usersByDni.put(u.getDni(), u));
        List<Group> loadedGroups = loadedGroupStorage.load(usersByDni);
        Map<String, Group> groupsById = new HashMap<>();
        loadedGroups.forEach(g -> groupsById.put(g.getIdentifier(), g));
        List<Ticket> loadedTickets = loadedTicketStorage.load(usersByDni, groupsById);
        List<Attraction> loadedAttractions = loadedAttractionStorage.load();

        users = loadedUsers;
        groupStorage = loadedGroupStorage;
        ticketStorage = loadedTicketStorage;
        attractionStorage = loadedAttractionStorage;
        replace(groups, loadedGroups);
        replace(tickets, loadedTickets);
        replace(attractions, loadedAttractions);
        currentDate = LocalDate.now(ZoneId.systemDefault());
        basePrice = DEFAULT_PRICE;
        warnings.clear();
        warnings.addAll(userStorage.getWarnings());
        warnings.addAll(loadedGroupStorage.getWarnings());
        warnings.addAll(loadedTicketStorage.getWarnings());
        warnings.addAll(loadedAttractionStorage.getWarnings());
    }

    private static <T> void replace(List<T> target, List<T> source) {
        target.clear();
        target.addAll(source);
    }

    public List<String> getLoadWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    // ---- system settings -------------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) throws OperationException {
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new OperationException("price must be a positive amount with at most two decimals");
        }
        basePrice = price.setScale(2);
    }

    // ---- users -----------------------------------------------------------------------------

    public UserManager getUsers() {
        return users;
    }

    public void createUser(User user) throws OperationException {
        users.create(user);
    }

    public void updateUser(String email, String password, UserField field, String value)
            throws OperationException {
        users.update(email, password, field, value);
    }

    /**
     * Deletes a user unless an active ticket or a locked group prevents it. The user is
     * also removed from every group and from any attraction they are still inside.
     */
    public void deleteUser(String email, String password) throws OperationException {
        User user = users.authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new OperationException("user has an active ticket");
        }
        List<Group> memberOf = groups.stream().filter(g -> g.hasMember(user)).toList();
        for (Group g : memberOf) {
            if (isLocked(g)) {
                throw new OperationException("user belongs to group " + g.getIdentifier()
                        + ", which is locked by an active group ticket");
            }
        }
        Map<Group, Integer> positions = new HashMap<>();
        memberOf.forEach(g -> positions.put(g, g.removeMember(user)));
        List<Attraction> inside = attractions.stream().filter(a -> a.isInside(user)).toList();
        inside.forEach(a -> a.release(user));
        Runnable undo = () -> {
            positions.forEach((g, index) -> g.addMember(index, user));
            inside.forEach(a -> a.admit(List.of(user)));
        };
        persist(undo, this::saveGroups, this::saveTickets);
        try {
            users.remove(user);
        } catch (OperationException e) {
            undo.run();
            bestEffort(this::saveGroups);
            throw e;
        }
    }

    private boolean hasActiveTicket(User user) {
        return tickets.stream()
                .filter(t -> t.isActiveOn(currentDate))
                .anyMatch(t -> t.getUsers().stream().anyMatch(u -> u == user));
    }

    // ---- groups ----------------------------------------------------------------------------

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group findGroup(String identifier) {
        return groups.stream().filter(g -> g.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * A group is locked while any of its group tickets is dated today or later.
     */
    public boolean isLocked(Group group) {
        return tickets.stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .anyMatch(t -> t.getGroup() == group && t.isActiveOn(currentDate));
    }

    public void createGroup(String identifier, String name) throws OperationException {
        if (findGroup(identifier) != null) {
            throw new OperationException("group " + identifier + " already exists");
        }
        Group group = new Group(identifier, name);
        groups.add(group);
        persist(() -> groups.remove(group), this::saveGroups);
    }

    public void addToGroup(String identifier, String email) throws OperationException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (group.hasMember(user)) {
            throw new OperationException(email + " is already a member of group " + identifier);
        }
        group.addMember(user);
        persist(() -> group.removeMember(user), this::saveGroups);
    }

    public void removeFromGroup(String identifier, String email) throws OperationException {
        Group group = requireModifiableGroup(identifier);
        User user = group.getMembers().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new OperationException(email + " is not a member of group " + identifier));
        int index = group.removeMember(user);
        persist(() -> group.addMember(index, user), this::saveGroups);
    }

    public void deleteGroup(String identifier) throws OperationException {
        Group group = requireModifiableGroup(identifier);
        int index = groups.indexOf(group);
        List<GroupTicket> historical = groupTicketsOf(group);
        groups.remove(index);
        historical.forEach(t -> t.setGroup(null));
        persist(() -> {
            groups.add(index, group);
            historical.forEach(t -> t.setGroup(group));
        }, this::saveGroups, this::saveTickets);
    }

    private List<GroupTicket> groupTicketsOf(Group group) {
        return tickets.stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(t -> t.getGroup() == group)
                .toList();
    }

    private Group requireGroup(String identifier) throws OperationException {
        Group group = findGroup(identifier);
        if (group == null) {
            throw new OperationException("no group " + identifier);
        }
        return group;
    }

    private Group requireModifiableGroup(String identifier) throws OperationException {
        Group group = requireGroup(identifier);
        if (isLocked(group)) {
            throw new OperationException("group " + identifier + " is locked by an active group ticket");
        }
        return group;
    }

    private User requireUser(String email) throws OperationException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new OperationException("no user with email " + email);
        }
        return user;
    }

    // ---- tickets ---------------------------------------------------------------------------

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /**
     * Looks a ticket up in the shared identifier space, including group compositions.
     */
    public Ticket findTicket(String identifier) {
        for (Ticket t : tickets) {
            if (t.getIdentifier().equals(identifier)) {
                return t;
            }
            if (t instanceof GroupTicket g) {
                for (IndividualTicket part : g.getComposition()) {
                    if (part.getIdentifier().equals(identifier)) {
                        return part;
                    }
                }
            }
        }
        return null;
    }

    private void requireFreeIdentifier(String identifier) throws OperationException {
        if (findTicket(identifier) != null) {
            throw new OperationException("a ticket with identifier " + identifier + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) throws OperationException {
        if (date.isBefore(currentDate)) {
            throw new OperationException("date " + date + " is before the current date " + currentDate);
        }
    }

    public IndividualTicket createTicket(String identifier, String email, LocalDate date)
            throws OperationException {
        requireFreeIdentifier(identifier);
        User user = requireUser(email);
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, basePrice, 0);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket), this::saveTickets);
        return ticket;
    }

    public void deleteTicket(String identifier) throws OperationException {
        Ticket ticket = findTicket(identifier);
        if (ticket == null) {
            throw new OperationException("no ticket " + identifier);
        }
        if (ticket instanceof GroupTicket) {
            throw new OperationException(identifier + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (!tickets.contains(ticket)) {
            throw new OperationException(identifier + " belongs to a group ticket and cannot be deleted on its own");
        }
        removeFutureTicket(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, LocalDate date)
            throws OperationException {
        requireFreeIdentifier(identifier);
        Group group = requireGroup(groupIdentifier);
        if (group.getMembers().isEmpty()) {
            throw new OperationException("group " + groupIdentifier + " has no members");
        }
        requireNotPast(date);
        List<User> members = group.getMembers();
        for (User member : members) {
            requireFreeIdentifier(GroupTicket.compositionIdentifier(identifier, member));
        }
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = members.stream()
                .map(m -> new IndividualTicket(GroupTicket.compositionIdentifier(identifier, m), date, m,
                        basePrice, discount))
                .toList();
        GroupTicket ticket = new GroupTicket(identifier, date, group, composition);
        tickets.add(ticket);
        persist(() -> tickets.remove(ticket), this::saveTickets);
        return ticket;
    }

    public void deleteGroupTicket(String identifier) throws OperationException {
        if (!(findTicket(identifier) instanceof GroupTicket ticket)) {
            throw new OperationException("no group ticket " + identifier);
        }
        removeFutureTicket(ticket);
    }

    private void removeFutureTicket(Ticket ticket) throws OperationException {
        if (!ticket.getDate().isAfter(currentDate)) {
            throw new OperationException("ticket " + ticket.getIdentifier()
                    + " is for today or earlier and is kept as a historical record");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        persist(() -> tickets.add(index, ticket), this::saveTickets);
    }

    // ---- attractions -----------------------------------------------------------------------

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction findAttraction(String identifier) {
        return attractions.stream().filter(a -> a.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public Attraction requireAttraction(String identifier) throws OperationException {
        Attraction attraction = findAttraction(identifier);
        if (attraction == null) {
            throw new OperationException("no attraction " + identifier);
        }
        return attraction;
    }

    public void createAttraction(String identifier, String name, int capacity) throws OperationException {
        if (findAttraction(identifier) != null) {
            throw new OperationException("attraction " + identifier + " already exists");
        }
        if (capacity <= 0) {
            throw new OperationException("capacity must be a positive whole number");
        }
        Attraction attraction = new Attraction(identifier, name, capacity);
        attractions.add(attraction);
        persist(() -> attractions.remove(attraction), this::saveAttractions);
    }

    /**
     * Admits every user of the ticket together, or nobody. Returns how many entered.
     */
    public int enter(String attractionIdentifier, String ticketIdentifier) throws OperationException {
        Attraction attraction = requireAttraction(attractionIdentifier);
        Ticket ticket = findTicket(ticketIdentifier);
        if (ticket == null) {
            throw new OperationException("no ticket " + ticketIdentifier);
        }
        if (!ticket.isValidOn(currentDate)) {
            throw new OperationException("ticket " + ticketIdentifier + " is valid on " + ticket.getDate()
                    + ", not on " + currentDate);
        }
        List<User> entrants = ticket.getUsers();
        for (User user : entrants) {
            if (!users.isRegistered(user)) {
                throw new OperationException(user.getEmail() + " is no longer a registered user");
            }
            if (attraction.isInside(user)) {
                throw new OperationException(user.getEmail() + " is already inside " + attractionIdentifier);
            }
        }
        if (entrants.size() > attraction.getAvailablePlaces()) {
            throw new OperationException("not enough places in " + attractionIdentifier + ": "
                    + attraction.getAvailablePlaces() + " available, " + entrants.size() + " needed");
        }
        attraction.admit(entrants);
        return entrants.size();
    }

    public void exit(String attractionIdentifier, String email) throws OperationException {
        Attraction attraction = requireAttraction(attractionIdentifier);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new OperationException(email + " is not inside " + attractionIdentifier));
        attraction.release(user);
    }

    // ---- persistence -----------------------------------------------------------------------

    @FunctionalInterface
    private interface Save {
        void run() throws IOException;
    }

    private void saveGroups() throws IOException {
        groupStorage.save(groups);
    }

    private void saveTickets() throws IOException {
        ticketStorage.save(tickets);
    }

    private void saveAttractions() throws IOException {
        attractionStorage.save(attractions);
    }

    /**
     * Saves the given files; if any save fails, reverts the in-memory change, rewrites
     * the previous state of those files and rejects the operation.
     */
    private static void persist(Runnable undo, Save... saves) throws OperationException {
        try {
            for (Save save : saves) {
                save.run();
            }
        } catch (IOException e) {
            undo.run();
            for (Save save : saves) {
                bestEffort(save);
            }
            throw new OperationException("could not save park data: " + e.getMessage());
        }
    }

    private static void bestEffort(Save save) {
        try {
            save.run();
        } catch (IOException ignored) {
            // the original failure is the one reported
        }
    }
}
