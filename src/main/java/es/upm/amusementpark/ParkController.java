package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central park management. Only one instance exists per execution; it keeps
 * the current system date, the current base ticket price, and centralizes
 * access to the users, groups, tickets and attractions of the park.
 */
public final class ParkController {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    private ParkController() {
        reset();
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Restores the start-of-execution state (used by tests). */
    void reset() {
        currentDate = LocalDate.now();
        basePrice = DEFAULT_PRICE;
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the current system date from a {@code yyyy-MM-dd} text.
     *
     * @throws IllegalArgumentException if the text is not a valid date in that format
     */
    public void setCurrentDate(String text) {
        currentDate = parseDate(text);
    }

    /**
     * Parses a {@code yyyy-MM-dd} date strictly.
     *
     * @throws IllegalArgumentException if the text is not a valid date in that format
     */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date (expected yyyy-MM-dd): " + text, e);
        }
    }

    /**
     * Sets the current base ticket price.
     *
     * @throws IllegalArgumentException if the text is not a positive amount with at most two decimals
     */
    public void setBasePrice(String text) {
        basePrice = parsePrice(text.trim());
    }

    private static BigDecimal parsePrice(String text) {
        int digits = 0;
        int decimals = 0;
        boolean seenPoint = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.' && !seenPoint) {
                seenPoint = true;
            } else if (c >= '0' && c <= '9') {
                digits++;
                if (seenPoint) {
                    decimals++;
                }
            } else {
                throw new IllegalArgumentException("invalid price: " + text);
            }
        }
        if (digits == 0 || decimals > 2 || text.startsWith(".") || text.endsWith(".")) {
            throw new IllegalArgumentException("invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive: " + text);
        }
        return price;
    }

    // --- users -----------------------------------------------------------

    /** Adds a user to the park's user directory. */
    public void registerUser(User user) {
        users.put(user.getEmail(), user);
    }

    /** @return the registered user with that email, or {@code null} */
    public User findUser(String email) {
        return users.get(email);
    }

    // --- groups ----------------------------------------------------------

    public Group findGroup(String identifier) {
        return groups.get(identifier);
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public Group createGroup(String identifier, String name) {
        String id = requireText(identifier, "group identifier");
        String groupName = requireText(name, "group name");
        if (groups.containsKey(id)) {
            throw new IllegalArgumentException("group already exists: " + id);
        }
        Group group = new Group(id, groupName);
        groups.put(id, group);
        return group;
    }

    public void addUserToGroup(String identifier, String email) {
        Group group = existingGroup(identifier);
        User user = existingUser(email);
        requireModifiable(group);
        if (!group.addMember(user)) {
            throw new IllegalArgumentException("user " + user.getEmail()
                    + " already belongs to group " + group.getIdentifier());
        }
    }

    public void removeUserFromGroup(String identifier, String email) {
        Group group = existingGroup(identifier);
        String address = requireText(email, "email");
        requireModifiable(group);
        User user = users.get(address);
        if (user == null || !group.removeMember(user)) {
            throw new IllegalArgumentException("user " + address
                    + " does not belong to group " + group.getIdentifier());
        }
    }

    public void deleteGroup(String identifier) {
        Group group = existingGroup(identifier);
        if (hasActiveGroupTicket(group)) {
            throw new IllegalArgumentException("group " + group.getIdentifier()
                    + " has a group ticket for the current date or later");
        }
        for (GroupTicket ticket : groupTicketsOf(group)) {
            ticket.releaseGroup();
        }
        groups.remove(group.getIdentifier());
    }

    // --- attractions -----------------------------------------------------

    public Attraction findAttraction(String identifier) {
        return attractions.get(identifier);
    }

    public Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    public Attraction createAttraction(String identifier, String name, String capacityText) {
        String id = requireText(identifier, "attraction identifier");
        String attractionName = requireText(name, "attraction name");
        int capacity = parseCapacity(capacityText);
        if (attractions.containsKey(id)) {
            throw new IllegalArgumentException("attraction already exists: " + id);
        }
        Attraction attraction = new Attraction(id, attractionName, capacity);
        attractions.put(id, attraction);
        return attraction;
    }

    public Attraction existingAttraction(String identifier) {
        String id = requireText(identifier, "attraction identifier");
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new IllegalArgumentException("attraction not found: " + id);
        }
        return attraction;
    }

    /**
     * Lets the users admitted by a ticket enter an attraction. The ticket must
     * be for the current date and the attraction must have room for all of
     * them; otherwise nobody enters.
     *
     * @return the users who entered
     */
    public List<User> enterAttraction(String attractionIdentifier, String ticketIdentifier) {
        Attraction attraction = existingAttraction(attractionIdentifier);
        Ticket ticket = existingTopLevelTicket(ticketIdentifier);
        if (ticket instanceof GroupTicket) {
            throw new IllegalArgumentException("access with group tickets is not available yet: "
                    + ticket.getIdentifier());
        }
        if (!ticket.getDate().equals(currentDate)) {
            throw new IllegalArgumentException("ticket " + ticket.getIdentifier() + " is valid on "
                    + ticket.getDate() + ", not on the current date " + currentDate);
        }
        List<User> entrants = ticket.admittedUsers();
        for (User user : entrants) {
            if (attraction.isInside(user)) {
                throw new IllegalArgumentException("user " + user.getEmail()
                        + " is already inside attraction " + attraction.getIdentifier());
            }
        }
        if (attraction.getOccupants().size() + entrants.size() > attraction.getCapacity()) {
            throw new IllegalArgumentException("attraction " + attraction.getIdentifier()
                    + " does not have enough capacity");
        }
        for (User user : entrants) {
            attraction.addOccupant(user);
        }
        return entrants;
    }

    private Ticket existingTopLevelTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket candidate : tickets.values()) {
            if (candidate.identifiers().contains(id)) {
                throw new IllegalArgumentException("ticket " + id + " belongs to group ticket "
                        + candidate.getIdentifier() + "; use the group ticket");
            }
        }
        throw new IllegalArgumentException("ticket not found: " + id);
    }

    private static int parseCapacity(String text) {
        String value = text == null ? "" : text.trim();
        boolean digitsOnly = !value.isEmpty();
        for (int i = 0; i < value.length() && digitsOnly; i++) {
            digitsOnly = value.charAt(i) >= '0' && value.charAt(i) <= '9';
        }
        int capacity = 0;
        if (digitsOnly) {
            try {
                capacity = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                capacity = 0;
            }
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be a whole number greater than zero: " + text);
        }
        return capacity;
    }

    // --- tickets ---------------------------------------------------------

    public Ticket findTicket(String identifier) {
        return tickets.get(identifier);
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    /** Common check of the identifier space shared by every ticket type. */
    public boolean isTicketIdentifierUsed(String identifier) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.identifiers().contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    public IndividualTicket createIndividualTicket(String identifier, String email, String dateText) {
        String id = requireText(identifier, "ticket identifier");
        User user = existingUser(email);
        LocalDate date = parseDate(dateText);
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, user, date, basePrice);
        addTicket(ticket);
        return ticket;
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String dateText) {
        String id = requireText(identifier, "ticket identifier");
        Group group = existingGroup(groupIdentifier);
        LocalDate date = parseDate(dateText);
        requireNotPast(date);
        Set<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalArgumentException("group " + group.getIdentifier() + " has no members");
        }
        BigDecimal price = GroupTicket.discountedPrice(basePrice, members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(
                    GroupTicket.componentIdentifier(id, member.getDni()), member, date, price));
        }
        GroupTicket ticket = new GroupTicket(id, group, date, composition);
        addTicket(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        if (!(tickets.get(id) instanceof GroupTicket ticket)) {
            throw new IllegalArgumentException("group ticket not found: " + id);
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new IllegalArgumentException("group ticket " + id
                    + " is for the current date or earlier and must be kept");
        }
        tickets.remove(id);
    }

    /**
     * A user may be deleted only without active tickets (including those inside
     * group tickets) and when every group they belong to can be modified.
     */
    public boolean canDeleteUser(User user) {
        if (hasActiveTicket(user)) {
            return false;
        }
        for (Group group : groups.values()) {
            if (group.hasMember(user) && hasActiveGroupTicket(group)) {
                return false;
            }
        }
        return true;
    }

    public void deleteTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        Ticket ticket = tickets.get(id);
        if (ticket instanceof GroupTicket) {
            throw new IllegalArgumentException("ticket " + id
                    + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (ticket == null) {
            for (Ticket candidate : tickets.values()) {
                if (candidate.identifiers().contains(id)) {
                    throw new IllegalArgumentException("ticket " + id + " belongs to group ticket "
                            + candidate.getIdentifier() + " and cannot be deleted directly");
                }
            }
            throw new IllegalArgumentException("ticket not found: " + id);
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new IllegalArgumentException("ticket " + id
                    + " is for the current date or earlier and must be kept");
        }
        tickets.remove(id);
    }

    /** Tickets of a user, obtained by traversing the tickets managed by the park. */
    public List<Ticket> ticketsOf(User user) {
        return tickets.values().stream().filter(t -> t.involves(user)).toList();
    }

    /** Derived state: whether the user has a ticket for the current date or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : ticketsOf(user)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private void addTicket(Ticket ticket) {
        Set<String> seen = new HashSet<>();
        for (String id : ticket.identifiers()) {
            if (isTicketIdentifierUsed(id) || !seen.add(id)) {
                throw new IllegalArgumentException("ticket identifier already in use: " + id);
            }
        }
        tickets.put(ticket.getIdentifier(), ticket);
    }

    private void requireNotPast(LocalDate date) {
        if (date.isBefore(currentDate)) {
            throw new IllegalArgumentException("date " + date
                    + " is earlier than the current date " + currentDate);
        }
    }

    /**
     * A group has an active group ticket when one of its group tickets is
     * dated on or after the current system date. The state is always computed
     * from the tickets, never stored.
     */
    public boolean hasActiveGroupTicket(Group group) {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    private void requireModifiable(Group group) {
        if (hasActiveGroupTicket(group)) {
            throw new IllegalArgumentException("group " + group.getIdentifier()
                    + " cannot be modified while it has an active group ticket");
        }
    }

    private Group existingGroup(String identifier) {
        String id = requireText(identifier, "group identifier");
        Group group = groups.get(id);
        if (group == null) {
            throw new IllegalArgumentException("group not found: " + id);
        }
        return group;
    }

    private User existingUser(String email) {
        String address = requireText(email, "email");
        User user = users.get(address);
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + address);
        }
        return user;
    }

    private static String requireText(String value, String what) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(what + " must not be empty");
        }
        return text;
    }
}
