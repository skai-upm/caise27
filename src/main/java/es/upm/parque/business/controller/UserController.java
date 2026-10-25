package es.upm.parque.business.controller;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.repository.UserRepository;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.business.validation.UserValidator;

/**
 * Business rules for users: validation, uniqueness, create, delete and update. Singleton.
 * Rejected operations throw {@link IllegalArgumentException} with the reason.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private final List<User> users = new ArrayList<>();
    private UserRepository repository;
    private PasswordHasher hasher;
    private ParkController park;
    private GroupController groups;
    private TicketController tickets;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository repository, PasswordHasher hasher, ParkController park,
                          GroupController groups, TicketController tickets) {
        this.repository = repository;
        this.hasher = hasher;
        this.park = park;
        this.groups = groups;
        this.tickets = tickets;
    }

    /**
     * Reloads the users, hashing any legacy plaintext password found in storage.
     */
    public void load() {
        users.clear();
        users.addAll(repository.load());
        boolean migrated = false;
        for (User u : users) {
            if (!hasher.isHash(u.getPasswordHash())) {
                u.setPasswordHash(hasher.hash(u.getPasswordHash()));
                migrated = true;
            }
        }
        if (migrated) {
            repository.save(users);
        }
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equalsIgnoreCase(dni)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Like {@link #findByEmail(String)} but failing when the user does not exist.
     */
    public User get(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return user;
    }

    public User create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        requireNotEmpty(name, "name");
        requireNotEmpty(surname, "surname");
        requireNotEmpty(phone, "phone");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("Invalid DNI: " + dni);
        }
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        if (findByDni(dni) != null) {
            throw new IllegalArgumentException("DNI already registered: " + dni);
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = new User(name, surname, dni.toUpperCase(), email, hasher.hash(password), phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    /**
     * Deletes a user after checking credentials and that no ticket dated on or after the system
     * date belongs to it; the user also leaves every group.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        if (tickets.hasCurrentOrFutureTicket(user)) {
            throw new IllegalArgumentException("User " + email + " has tickets dated on or after "
                    + park.getSystemDate());
        }
        groups.removeUserEverywhere(user);
        users.remove(user);
        repository.save(users);
    }

    public void update(String email, String password, String fieldText, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromText(fieldText);
        requireNotEmpty(newValue, field.name());
        switch (field) {
            case NAME -> user.setName(newValue);
            case SURNAME -> user.setSurname(newValue);
            case EMAIL -> {
                validateEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new IllegalArgumentException("Email already registered: " + newValue);
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                validatePassword(newValue);
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                validateCard(newValue);
                user.setCard(newValue);
            }
        }
        repository.save(users);
    }

    public User authenticate(String email, String password) {
        User user = get(email);
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password");
        }
        return user;
    }

    private static void requireNotEmpty(String value, String fieldName) {
        if (!UserValidator.isNotEmpty(value)) {
            throw new IllegalArgumentException("Empty value for " + fieldName);
        }
    }

    private static void validateEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    private static void validatePassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void validateCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("Invalid card number: it must have exactly 16 digits");
        }
    }
}
