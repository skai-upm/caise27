package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management: the only object holding users, groups, tickets and attractions,
 * together with the current system date and base ticket price.
 *
 * <p>Exactly one instance exists ({@link #getInstance()}). Every mutating operation validates
 * first, then changes memory and saves; if saving fails the change is undone.</p>
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = Money.of(new BigDecimal("40"));

    private static final Park INSTANCE = new Park();

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private ParkRepository repository;
    private LocalDate today;
    private BigDecimal basePrice;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Starts an execution: loads persisted state from the repository, sets the system date to
     * {@code startDate} and the base price to 40.00. Attractions start empty.
     */
    public void start(ParkRepository parkRepository, LocalDate startDate) {
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
        this.repository = parkRepository;
        this.today = startDate;
        this.basePrice = INITIAL_PRICE;

        Map<String, User> usersByDni = new LinkedHashMap<>();
        for (User u : parkRepository.users().load()) {
            users.put(u.getEmail(), u);
            usersByDni.put(u.getDni(), u);
        }
        for (Group g : parkRepository.groups().load(usersByDni)) {
            groups.put(g.getId(), g);
        }
        for (Ticket t : parkRepository.tickets().load(usersByDni, groups)) {
            tickets.put(t.getId(), t);
        }
        for (Attraction a : parkRepository.attractions().load()) {
            attractions.put(a.getId(), a);
        }
    }

    // ---------------------------------------------------------------- system settings

    public LocalDate getToday() {
        return today;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setToday(String date) {
        this.today = ValueParser.parseDate(date);
    }

    public void setBasePrice(String price) {
        this.basePrice = ValueParser.parsePrice(price);
    }

    // ---------------------------------------------------------------- queries

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User findUser(String email) {
        return users.get(email);
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    public Attraction findAttraction(String id) {
        return attractions.get(id);
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    /** Finds any ticket in the shared identifier space, including group-ticket constituents. */
    public Ticket findTicket(String id) {
        Ticket top = tickets.get(id);
        if (top != null) {
            return top;
        }
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g) {
                for (IndividualTicket c : g.getComposition()) {
                    if (c.getId().equals(id)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    /** A group is locked while any of its group tickets is dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group && g.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** A user is locked while any ticket that admits them is dated today or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket t : tickets.values()) {
            if (t.isActiveOn(today) && t.getHolders().contains(user)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- users

    public User createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        requireNotBlank(firstName, surnames, dni, email, password, phone, card);
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("Invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number");
        }
        for (User u : users.values()) {
            if (u.getDni().equals(dni)) {
                throw new UserException("DNI already registered");
            }
        }
        if (users.containsKey(email)) {
            throw new UserException("Email already registered");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.put(email, user);
        commit(() -> users.remove(email));
        return user;
    }

    public void deleteUser(String email, String password) {
        User user = authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new UserException("User has active tickets");
        }
        List<Group> memberships = new ArrayList<>();
        for (Group g : groups.values()) {
            if (g.contains(user)) {
                if (isGroupLocked(g)) {
                    throw new UserException("User belongs to locked group " + g.getId());
                }
                memberships.add(g);
            }
        }
        Map<String, User> before = new LinkedHashMap<>(users);
        Map<Group, Integer> positions = new LinkedHashMap<>();
        for (Group g : memberships) {
            positions.put(g, g.remove(user));
        }
        users.remove(user.getEmail());
        commit(() -> {
            restore(users, before);
            positions.forEach((g, index) -> g.add(index, user));
        });
    }

    public void updateUser(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("Field cannot be modified: " + fieldName);
        }
        requireNotBlank(newValue);
        validateNewValue(user, field, newValue);

        Map<String, User> before = new LinkedHashMap<>(users);
        String previous = apply(user, field, newValue);
        if (field == UserField.EMAIL) {
            users.remove(email);
            users.put(newValue, user);
        }
        commit(() -> {
            apply(user, field, previous);
            restore(users, before);
        });
    }

    private void validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserException("Invalid email");
                }
                User other = users.get(value);
                if (other != null && other != user) {
                    throw new UserException("Email already registered");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserException("Invalid password");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserException("Invalid card number");
                }
            }
            default -> {
                // NAME, SURNAME and PHONE have no format restriction beyond being non-blank.
            }
        }
    }

    /** Sets the field and returns its previous value. */
    private static String apply(User user, UserField field, String value) {
        String previous;
        switch (field) {
            case NAME -> {
                previous = user.getFirstName();
                user.setFirstName(value);
            }
            case SURNAME -> {
                previous = user.getSurnames();
                user.setSurnames(value);
            }
            case EMAIL -> {
                previous = user.getEmail();
                user.setEmail(value);
            }
            case PASSWORD -> {
                previous = user.getPassword();
                user.setPassword(value);
            }
            case PHONE -> {
                previous = user.getPhone();
                user.setPhone(value);
            }
            case CARD -> {
                previous = user.getCard();
                user.setCard(value);
            }
            default -> throw new IllegalStateException("Unhandled field " + field);
        }
        return previous;
    }

    private User authenticate(String email, String password) {
        User user = users.get(email);
        if (user == null) {
            throw new UserException("User not found");
        }
        if (!user.checkPassword(password)) {
            throw new UserException("Incorrect password");
        }
        return user;
    }

    // ---------------------------------------------------------------- groups

    public Group createGroup(String id, String name) {
        requireNotBlank(id, name);
        if (groups.containsKey(id)) {
            throw new ParkException("Group already exists: " + id);
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        commit(() -> groups.remove(id));
        return group;
    }

    public void addToGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User user = users.get(email);
        if (user == null) {
            throw new ParkException("User not found: " + email);
        }
        if (group.contains(user)) {
            throw new ParkException("User already in group: " + email);
        }
        group.add(user);
        commit(() -> group.remove(user));
    }

    public void removeFromGroup(String groupId, String email) {
        Group group = requireModifiableGroup(groupId);
        User member = group.findMember(email);
        if (member == null) {
            throw new ParkException("User not in group: " + email);
        }
        int index = group.remove(member);
        commit(() -> group.add(index, member));
    }

    public void deleteGroup(String groupId) {
        Group group = requireModifiableGroup(groupId);
        Map<String, Group> before = new LinkedHashMap<>(groups);
        List<GroupTicket> historical = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group) {
                historical.add(g);
                g.clearGroup();
            }
        }
        groups.remove(groupId);
        commit(() -> {
            restore(groups, before);
            historical.forEach(g -> g.restoreGroup(group));
        });
    }

    private Group requireModifiableGroup(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new ParkException("Group not found: " + groupId);
        }
        if (isGroupLocked(group)) {
            throw new ParkException("Group is locked by an active group ticket: " + groupId);
        }
        return group;
    }

    // ---------------------------------------------------------------- tickets

    public IndividualTicket createTicket(String id, String email, String date) {
        requireNotBlank(id);
        requireFreeTicketId(id);
        User user = users.get(email);
        if (user == null) {
            throw new ParkException("User not found: " + email);
        }
        LocalDate day = requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, day, user, basePrice, 0);
        tickets.put(id, ticket);
        commit(() -> tickets.remove(id));
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new ParkException(findTicket(id) != null
                    ? "Ticket belongs to a group ticket and cannot be deleted on its own: " + id
                    : "Ticket not found: " + id);
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new ParkException("Not an individual ticket (use GROUPTICKET DELETE): " + id);
        }
        removeFutureTicket(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String date) {
        requireNotBlank(id);
        requireFreeTicketId(id);
        Group group = groups.get(groupId);
        if (group == null) {
            throw new ParkException("Group not found: " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ParkException("Group has no members: " + groupId);
        }
        LocalDate day = requireNotPast(date);
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String componentId = GroupTicket.componentId(id, member.getDni());
            requireFreeTicketId(componentId);
            composition.add(new IndividualTicket(componentId, day, member, basePrice, discount));
        }
        GroupTicket ticket = new GroupTicket(id, day, group, composition);
        tickets.put(id, ticket);
        commit(() -> tickets.remove(id));
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new ParkException("Group ticket not found: " + id);
        }
        removeFutureTicket(ticket);
    }

    private void removeFutureTicket(Ticket ticket) {
        if (!ticket.isDeletableOn(today)) {
            throw new ParkException("Only tickets dated after " + ValueParser.formatDate(today) + " can be deleted");
        }
        Map<String, Ticket> before = new LinkedHashMap<>(tickets);
        tickets.remove(ticket.getId());
        commit(() -> restore(tickets, before));
    }

    private void requireFreeTicketId(String id) {
        if (findTicket(id) != null) {
            throw new ParkException("Ticket identifier already in use: " + id);
        }
    }

    private LocalDate requireNotPast(String date) {
        LocalDate day = ValueParser.parseDate(date);
        if (day.isBefore(today)) {
            throw new ParkException("Date is in the past: " + date);
        }
        return day;
    }

    // ---------------------------------------------------------------- attractions

    public Attraction createAttraction(String id, String name, String capacity) {
        requireNotBlank(id, name);
        if (attractions.containsKey(id)) {
            throw new ParkException("Attraction already exists: " + id);
        }
        Attraction attraction = new Attraction(id, name, ValueParser.parseCapacity(capacity));
        attractions.put(id, attraction);
        commit(() -> attractions.remove(id));
        return attraction;
    }

    /** Admits every holder of the ticket at once; returns how many users entered. */
    public int enter(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new ParkException("Ticket not found: " + ticketId);
        }
        if (!ticket.isValidOn(today)) {
            throw new ParkException("Ticket " + ticketId + " is not valid on " + ValueParser.formatDate(today));
        }
        List<User> holders = ticket.getHolders();
        attraction.enter(holders);
        return holders.size();
    }

    public void exit(String attractionId, String email) {
        requireAttraction(attractionId).exit(email);
    }

    public Attraction requireAttraction(String attractionId) {
        Attraction attraction = attractions.get(attractionId);
        if (attraction == null) {
            throw new ParkException("Attraction not found: " + attractionId);
        }
        return attraction;
    }

    // ---------------------------------------------------------------- persistence helpers

    private void commit(Runnable undo) {
        try {
            repository.save(users.values(), groups.values(), tickets.values(), attractions.values());
        } catch (ParkException e) {
            undo.run();
            throw e;
        }
    }

    private static <K, V> void restore(Map<K, V> target, Map<K, V> snapshot) {
        target.clear();
        target.putAll(snapshot);
    }

    private static void requireNotBlank(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) {
                throw new UserException("Malformed command: empty field");
            }
        }
    }
}
