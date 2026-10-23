package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.TransactionManager;
import es.upm.amusementpark.persistence.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages park users: validation, uniqueness, credentials and the user
 * commands. Users are persisted through the injected {@link UserRepository}.
 */
public final class UserController extends AbstractController {

    private static final UserController INSTANCE = new UserController();

    private final Map<String, User> usersByEmail = new LinkedHashMap<>();
    private UserRepository repository;
    private TicketController tickets;
    private GroupController groups;
    private AttractionController attractions;
    private PasswordHasher hasher;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the collaborating controllers. */
    public void configure(UserRepository repository, TransactionManager transactions, PasswordHasher hasher,
                          TicketController tickets, GroupController groups, AttractionController attractions) {
        this.repository = repository;
        this.hasher = hasher;
        setTransactions(transactions);
        this.tickets = tickets;
        this.groups = groups;
        this.attractions = attractions;
        usersByEmail.clear();
    }

    /** Replaces the in-memory users with the stored ones. */
    void load() throws IOException {
        usersByEmail.clear();
        for (User user : repository.loadAll()) {
            usersByEmail.put(key(user.getEmail()), user);
        }
    }

    void save() throws IOException {
        repository.saveAll(usersByEmail.values());
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }

    public User findByEmail(String email) {
        return email == null ? null : usersByEmail.get(key(email));
    }

    public User findByDni(String dni) {
        for (User user : usersByEmail.values()) {
            if (user.getDni().equalsIgnoreCase(dni)) {
                return user;
            }
        }
        return null;
    }

    /** Whether {@code user} is this very registered user object. */
    public boolean isActive(User user) {
        return findByEmail(user.getEmail()) == user;
    }

    public User require(String email) throws NotFoundException {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("No user with email " + email);
        }
        return user;
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ParkException {
        requireNonBlank(firstName, "First name");
        requireNonBlank(surnames, "Surnames");
        requireValidDni(dni);
        requireValidEmail(email);
        requireValidPassword(password);
        requireNonBlank(phone, "Phone");
        requireValidCard(card);

        String normalizedDni = dni.toUpperCase(Locale.ROOT);
        if (findByDni(normalizedDni) != null) {
            throw new OperationNotAllowedException("A user with DNI " + normalizedDni + " already exists");
        }
        if (usersByEmail.containsKey(key(email))) {
            throw new OperationNotAllowedException("A user with email " + email + " already exists");
        }

        String userKey = key(email);
        usersByEmail.put(userKey,
                new User(firstName, surnames, normalizedDni, email, hasher.hash(password), phone, card));
        persist(this::save, () -> usersByEmail.remove(userKey));
    }

    /**
     * Changes one field of the user identified by {@code email} and
     * {@code password}. The new value must satisfy the same rules as on
     * creation; a new password is stored as a fresh hash. The same user object is modified, so every reference to it
     * (groups, tickets, attractions) stays valid.
     */
    public void update(String email, String password, UserField field, String newValue) throws ParkException {
        User user = authenticate(email, password);
        validate(user, field, newValue);
        String previous = valueOf(user, field);
        apply(user, field, field == UserField.PASSWORD ? hasher.hash(newValue) : newValue);
        rekey();
        persist(this::save, () -> {
            apply(user, field, previous);
            rekey();
        });
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group,
     * removing them from their groups and from any attraction. Historical
     * tickets keep referencing the deleted user as a fiscal record.
     */
    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (tickets.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("User " + email + " has active tickets (dated "
                    + tickets.today() + " or later) and cannot be deleted");
        }
        Group locked = groups.lockedGroupOf(user);
        if (locked != null) {
            throw new OperationNotAllowedException("User " + email + " belongs to group " + locked.getId()
                    + ", which has an active group ticket");
        }
        Map<Group, Integer> memberships = groups.removeEverywhere(user);
        Runnable restoreSeat = attractions.removeOccupant(user);
        String userKey = key(user.getEmail());
        usersByEmail.remove(userKey);
        persist(() -> {
            save();
            groups.save();
        }, () -> {
            usersByEmail.put(userKey, user);
            rekey();
            groups.restoreMemberships(user, memberships);
            restoreSeat.run();
        });
    }

    /**
     * Returns the user with that email if the password matches.
     *
     * @throws NotFoundException if no user has that email
     * @throws OperationNotAllowedException if the password is wrong
     */
    public User authenticate(String email, String password) throws ParkException {
        User user = require(email);
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new OperationNotAllowedException("Incorrect password for " + email);
        }
        return user;
    }

    private void validate(User user, UserField field, String value) throws ParkException {
        switch (field) {
            case NAME -> requireNonBlank(value, "First name");
            case SURNAME -> requireNonBlank(value, "Surnames");
            case PHONE -> requireNonBlank(value, "Phone");
            case PASSWORD -> requireValidPassword(value);
            case CARD -> requireValidCard(value);
            case EMAIL -> {
                requireValidEmail(value);
                User owner = findByEmail(value);
                if (owner != null && owner != user) {
                    throw new OperationNotAllowedException("A user with email " + value + " already exists");
                }
            }
        }
    }

    private static String valueOf(User user, UserField field) {
        return switch (field) {
            case NAME -> user.getFirstName();
            case SURNAME -> user.getSurnames();
            case EMAIL -> user.getEmail();
            case PASSWORD -> user.getPasswordHash();
            case PHONE -> user.getPhone();
            case CARD -> user.getCard();
        };
    }

    private static void apply(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPasswordHash(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    /** Rebuilds the email index, keeping the order of the users. */
    private void rekey() {
        List<User> ordered = new ArrayList<>(usersByEmail.values());
        usersByEmail.clear();
        for (User user : ordered) {
            usersByEmail.put(key(user.getEmail()), user);
        }
    }

    private static String key(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private static void requireNonBlank(String value, String label) throws InvalidDataException {
        if (!UserValidator.isNonBlank(value)) {
            throw new InvalidDataException(label + " must not be empty");
        }
    }

    private static void requireValidDni(String dni) throws InvalidDataException {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("Invalid DNI: " + dni + " (expected 8 digits and the correct check letter)");
        }
    }

    private static void requireValidEmail(String email) throws InvalidDataException {
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("Invalid email: " + email + " (expected text@text.xxx)");
        }
    }

    private static void requireValidPassword(String password) throws InvalidDataException {
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException(
                    "Invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
        }
    }

    private static void requireValidCard(String card) throws InvalidDataException {
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("Invalid card number: " + card + " (expected exactly 16 digits)");
        }
    }
}
