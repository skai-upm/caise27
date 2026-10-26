package es.upm.parque.business.controller;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the registered users and applies the create, delete and update rules.
 * Every successful change is written through {@link UserRepository}.
 * Failures are reported as {@link IllegalArgumentException} with a user-facing message.
 */
public class UserManager {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    private static final UserManager INSTANCE = new UserManager();

    private UserRepository repository;
    private PasswordHasher hasher;

    private UserManager() {
    }

    public static UserManager getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators and reloads the users from the repository, replacing any previous state.
     * Plain-text passwords found in the repository (written by older versions) are replaced by hashes.
     */
    public UserManager configure(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
        usersByDni.clear();
        boolean migrated = false;
        for (User u : repository.load()) {
            if (!hasher.isHash(u.getPasswordHash())) {
                u.setPasswordHash(hasher.hash(u.getPasswordHash()));
                migrated = true;
            }
            usersByDni.put(u.getDni(), u);
        }
        if (migrated) {
            persist();
        }
        return this;
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(usersByDni.values());
    }

    public User create(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        requireNotBlank(name, "name");
        requireNotBlank(surname, "surname");
        requireNotBlank(phone, "phone");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("Invalid DNI: " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireValidCard(card);
        if (usersByDni.containsKey(dni)) {
            throw new IllegalArgumentException("A user with DNI " + dni + " already exists");
        }
        requireEmailFree(email, null);
        User user = new User(name, surname, dni, email, hasher().hash(password), phone, card);
        usersByDni.put(dni, user);
        persist();
        return user;
    }

    /** @return the deleted user */
    public User delete(String email, String password) {
        User user = authenticate(email, password);
        usersByDni.remove(user.getDni());
        persist();
        return user;
    }

    public void update(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(value, "name");
                user.setName(value);
            }
            case SURNAME -> {
                requireNotBlank(value, "surname");
                user.setSurname(value);
            }
            case EMAIL -> {
                requireValidEmail(value);
                requireEmailFree(value, user);
                user.setEmail(value);
            }
            case PASSWORD -> {
                requireValidPassword(value);
                user.setPasswordHash(hasher().hash(value));
            }
            case PHONE -> {
                requireNotBlank(value, "phone");
                user.setPhone(value);
            }
            case CARD -> {
                requireValidCard(value);
                user.setCard(value);
            }
        }
        persist();
    }

    /**
     * @return the user with that email if the password matches
     * @throws IllegalArgumentException if the user does not exist or the password is wrong
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        if (!hasher().matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password");
        }
        return user;
    }

    public User findByDni(String dni) {
        return usersByDni.get(dni);
    }

    public User findByEmail(String email) {
        for (User u : usersByDni.values()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    private void requireEmailFree(String email, User owner) {
        User existing = findByEmail(email);
        if (existing != null && existing != owner) {
            throw new IllegalArgumentException("A user with email " + email + " already exists");
        }
    }

    private static void requireNotBlank(String value, String field) {
        if (!UserValidator.isNotBlank(value)) {
            throw new IllegalArgumentException("The " + field + " cannot be empty");
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("Invalid card number: it must have exactly 16 digits");
        }
    }

    private PasswordHasher hasher() {
        if (hasher == null) {
            throw new IllegalStateException("UserManager is not configured");
        }
        return hasher;
    }

    private UserRepository repository() {
        if (repository == null) {
            throw new IllegalStateException("UserManager is not configured");
        }
        return repository;
    }

    private void persist() {
        repository().save(new ArrayList<>(usersByDni.values()));
    }
}
