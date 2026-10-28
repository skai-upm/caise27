package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
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
     * after the current system date. The state is derived, never stored; group
     * tickets do not exist yet, so every group is currently modifiable.
     */
    public boolean isGroupModifiable(Group group) {
        return true;
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
