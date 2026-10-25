package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.UserException;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.repository.UserRepository;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates, deletes and updates users (Singleton), enforcing validation, uniqueness, hashed
 * passwords and the active-ticket rule, and keeps the repository in sync after every change.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private final List<User> users = new ArrayList<>();
    private UserRepository repository;
    private PasswordHasher hasher;
    private ParkController park;
    private TicketController tickets;
    private GroupController groups;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository repository, PasswordHasher hasher, ParkController park,
                          TicketController tickets, GroupController groups) {
        this.repository = repository;
        this.hasher = hasher;
        this.park = park;
        this.tickets = tickets;
        this.groups = groups;
    }

    /**
     * Replaces the in-memory users with the stored ones, hashing any legacy plaintext password.
     */
    public void load() {
        users.clear();
        users.addAll(repository.load());
        boolean migrated = false;
        for (User user : users) {
            if (!hasher.isHash(user.getPasswordHash())) {
                user.setPasswordHash(hasher.hash(user.getPasswordHash()));
                migrated = true;
            }
        }
        if (migrated) {
            save();
        }
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public User findByEmail(String email) {
        return users.stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
    }

    public User findByDni(String dni) {
        return users.stream().filter(user -> user.getDni().equalsIgnoreCase(dni)).findFirst().orElse(null);
    }

    /**
     * Returns the registered user with that email or fails.
     */
    public User getByEmail(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new ParkException("no user with email " + email);
        }
        return user;
    }

    public boolean isRegistered(User user) {
        return findByDni(user.getDni()) == user;
    }

    public User create(String name, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNotBlank(name, "first name");
        requireNotBlank(surnames, "surnames");
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("invalid DNI " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "telephone");
        requireValidCard(card);
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            throw new UserException("a user with DNI " + normalizedDni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserException("a user with email " + email + " already exists");
        }
        User user = new User(name, surnames, normalizedDni, email, hasher.hash(password), phone, card);
        users.add(user);
        save();
        return user;
    }

    /**
     * Deletes the authenticated user unless they hold a ticket dated on or after the system date,
     * and removes them from their groups.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        if (tickets.hasActiveTicket(user)) {
            throw new ParkException("user " + email + " has tickets dated on or after " + park.getCurrentDate());
        }
        users.remove(user);
        save();
        groups.removeFromAll(user);
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(newValue, "first name");
                user.setName(newValue);
            }
            case SURNAME -> {
                requireNotBlank(newValue, "surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                requireValidEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new UserException("a user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValidPassword(newValue);
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> {
                requireNotBlank(newValue, "telephone");
                user.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                user.setCard(newValue);
            }
        }
        save();
    }

    /**
     * Returns the user with the given email if the password matches its stored hash.
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("no user with email " + email);
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new UserException("incorrect password");
        }
        return user;
    }

    private void save() {
        repository.saveAll(users);
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (!UserValidator.isNotBlank(value)) {
            throw new UserException(fieldName + " must not be empty");
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("invalid email " + email);
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("invalid card number: it must have exactly 16 digits");
        }
    }
}
