package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central park management. Only one instance exists per execution; it keeps
 * the current system date, the current base ticket price, and centralizes
 * access to the users and groups of the park.
 */
public final class ParkController {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();

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
        try {
            currentDate = LocalDate.parse(text.trim(), DATE_FORMAT);
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
        groups.remove(group.getIdentifier());
    }

    /**
     * A group has an active group ticket when one of its group tickets is
     * dated on or after the current system date. The state is always computed,
     * never stored. Group tickets are not part of the park yet, so no group
     * currently has one.
     */
    public boolean hasActiveGroupTicket(Group group) {
        return false;
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
