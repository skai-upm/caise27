package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Single central element maintaining the overall park state during an execution.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state. */
    void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Replaces the current system date.
     *
     * @throws IllegalArgumentException if the text is not a valid {@code yyyy-MM-dd} date
     */
    public void setCurrentDate(String text) {
        currentDate = parseDate(text);
    }

    /**
     * Replaces the current base ticket price.
     *
     * @throws IllegalArgumentException if the text is not a valid, positive monetary amount
     */
    public void setCurrentPrice(String text) {
        currentPrice = parsePrice(text);
    }

    /**
     * @throws IllegalArgumentException if a user with the same e-mail already exists
     */
    public void addUser(User user) {
        if (users.containsKey(user.getEmail())) {
            throw new IllegalArgumentException("User already exists: " + user.getEmail());
        }
        users.put(user.getEmail(), user);
    }

    public Optional<User> findUser(String email) {
        return Optional.ofNullable(users.get(email));
    }

    /**
     * Creates an empty group.
     *
     * @throws IllegalArgumentException if the identifier is already in use
     */
    public Group createGroup(String identifier, String name) {
        if (groups.containsKey(identifier)) {
            throw new IllegalArgumentException("Group already exists: " + identifier);
        }
        Group group = new Group(identifier, name);
        groups.put(identifier, group);
        return group;
    }

    public Optional<Group> findGroup(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    /**
     * A group is modifiable when it has no associated group ticket dated on or
     * after the current system date. The state is derived, never stored.
     */
    public boolean isGroupModifiable(Group group) {
        return findGroupTickets(group).stream().noneMatch(ticket -> ticket.isActive(currentDate));
    }

    /** Group tickets purchased by {@code group}, found by traversing the tickets. */
    public List<GroupTicket> findGroupTickets(Group group) {
        return tickets.values().stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(ticket -> ticket.getGroup() == group)
                .toList();
    }

    /**
     * Deletes a group without active group tickets. Its historical group tickets
     * are kept, with their group reference cleared.
     */
    public void deleteGroup(String identifier) {
        Group group = modifiableGroup(identifier);
        findGroupTickets(group).forEach(GroupTicket::detachGroup);
        groups.remove(identifier);
    }

    public void addUserToGroup(String identifier, String email) {
        Group group = modifiableGroup(identifier);
        User user = findUser(email).orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        group.addMember(user);
    }

    public void removeUserFromGroup(String identifier, String email) {
        modifiableGroup(identifier).removeMember(email);
    }

    private Group modifiableGroup(String identifier) {
        Group group = findGroup(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + identifier));
        if (!isGroupModifiable(group)) {
            throw new IllegalStateException("Group " + identifier + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    /**
     * Deletes a user. Rejected while the user has an active ticket or belongs to a
     * group that cannot be modified; otherwise the user is also removed from all
     * their groups. Historical tickets keep referencing the user.
     */
    public void deleteUser(String email) {
        User user = findUser(email).orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        if (hasActiveTickets(user)) {
            throw new IllegalStateException("User " + email + " has active tickets and cannot be deleted");
        }
        List<Group> memberships = groups.values().stream().filter(group -> group.hasMember(email)).toList();
        for (Group group : memberships) {
            if (!isGroupModifiable(group)) {
                throw new IllegalStateException("User " + email + " belongs to group " + group.getIdentifier()
                        + " which has an active group ticket");
            }
        }
        memberships.forEach(group -> group.removeMember(email));
        users.remove(email);
    }

    // ---- tickets ----

    /** Identifier lookup over the shared space of all tickets, including composed ones. */
    public boolean isTicketIdentifierInUse(String identifier) {
        return findAnyTicket(identifier).isPresent();
    }

    public Optional<Ticket> findAnyTicket(String identifier) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.getIdentifier().equals(identifier)) {
                return Optional.of(ticket);
            }
            for (IndividualTicket part : ticket.getIndividualTickets()) {
                if (part.getIdentifier().equals(identifier)) {
                    return Optional.of(part);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Ticket> findTicket(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    /**
     * Registers a ticket after checking, in a common manner for every ticket type,
     * that neither its identifier nor those of its individual tickets are in use.
     * Nothing is registered if any identifier conflicts.
     */
    void registerTicket(Ticket ticket) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(ticket.getIdentifier());
        for (IndividualTicket part : ticket.getIndividualTickets()) {
            if (part != ticket) {
                identifiers.add(part.getIdentifier());
            }
        }
        for (String identifier : identifiers) {
            if (isTicketIdentifierInUse(identifier) || identifiers.indexOf(identifier) != identifiers.lastIndexOf(identifier)) {
                throw new IllegalArgumentException("Ticket identifier already in use: " + identifier);
            }
        }
        tickets.put(ticket.getIdentifier(), ticket);
    }

    /**
     * Creates an individual ticket at the current base price.
     */
    public IndividualTicket createIndividualTicket(String identifier, String email, String dateText) {
        LocalDate date = parseDate(dateText);
        User user = findUser(email).orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        if (date.isBefore(currentDate)) {
            throw new IllegalArgumentException("Tickets cannot be created for past dates: " + date);
        }
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, currentPrice);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Deletes an individual ticket dated strictly after the current date.
     */
    public void deleteIndividualTicket(String identifier) {
        Ticket ticket = tickets.get(identifier);
        if (!(ticket instanceof IndividualTicket)) {
            if (isTicketIdentifierInUse(identifier)) {
                throw new IllegalStateException("Ticket " + identifier
                        + " is not an individual ticket or belongs to a group ticket and cannot be deleted directly");
            }
            throw new IllegalArgumentException("Ticket not found: " + identifier);
        }
        if (!ticket.isDeletable(currentDate)) {
            throw new IllegalStateException("Ticket " + identifier
                    + " is for the current day or an earlier date and cannot be deleted");
        }
        tickets.remove(identifier);
    }

    /**
     * Creates a group ticket for the current members of a non-empty group, with the
     * member-count discount applied to the current base price.
     */
    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String dateText) {
        LocalDate date = parseDate(dateText);
        Group group = findGroup(groupIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupIdentifier));
        if (group.getMembers().isEmpty()) {
            throw new IllegalArgumentException("Group " + groupIdentifier + " is empty");
        }
        if (date.isBefore(currentDate)) {
            throw new IllegalArgumentException("Tickets cannot be created for past dates: " + date);
        }
        GroupTicket ticket = GroupTicket.purchase(identifier, date, group, currentPrice);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Deletes a group ticket dated strictly after the current date, together with its composition.
     */
    public void deleteGroupTicket(String identifier) {
        Ticket ticket = tickets.get(identifier);
        if (!(ticket instanceof GroupTicket)) {
            throw new IllegalArgumentException("Group ticket not found: " + identifier);
        }
        if (!ticket.isDeletable(currentDate)) {
            throw new IllegalStateException("Group ticket " + identifier
                    + " is for the current day or an earlier date and cannot be deleted");
        }
        tickets.remove(identifier);
    }

    /** The individual tickets (standalone or composed) of a user, found by traversal. */
    public List<IndividualTicket> findTicketsOfUser(User user) {
        return tickets.values().stream()
                .flatMap(ticket -> ticket.getIndividualTickets().stream())
                .filter(part -> part.getUser() == user)
                .toList();
    }

    /** Derived lock state of a user: any associated ticket that is still active. */
    public boolean hasActiveTickets(User user) {
        return findTicketsOfUser(user).stream().anyMatch(ticket -> ticket.isActive(currentDate));
    }

    // ---- attractions ----

    /**
     * Creates an attraction with no occupants.
     *
     * @throws IllegalArgumentException for a duplicate identifier or a capacity that is not a positive integer
     */
    public Attraction createAttraction(String identifier, String name, String capacityText) {
        int capacity = parseCapacity(capacityText);
        if (attractions.containsKey(identifier)) {
            throw new IllegalArgumentException("Attraction already exists: " + identifier);
        }
        Attraction attraction = new Attraction(identifier, name, capacity);
        attractions.put(identifier, attraction);
        return attraction;
    }

    public Optional<Attraction> findAttraction(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    private static int parseCapacity(String text) {
        if (text == null || text.isEmpty() || !isDigits(text)) {
            throw new IllegalArgumentException("Invalid capacity, expected a positive integer: " + text);
        }
        try {
            int capacity = Integer.parseInt(text);
            if (capacity <= 0) {
                throw new IllegalArgumentException("Invalid capacity, expected a positive integer: " + text);
            }
            return capacity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid capacity, expected a positive integer: " + text);
        }
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            throw new IllegalArgumentException("Invalid date, expected yyyy-MM-dd: " + text);
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || !isDigits(integerPart) || (dot >= 0 && (decimals.isEmpty()
                || decimals.length() > 2 || !isDigits(decimals)))) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Invalid price, it must be positive: " + text);
        }
        return price;
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i)) || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
