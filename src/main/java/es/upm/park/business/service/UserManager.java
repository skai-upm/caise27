package es.upm.park.business.service;

import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.UserFileStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User operations: creation, authenticated deletion and update, with validation and
 * uniqueness rules. The storage is updated after every successful change; on failure
 * neither memory nor file change.
 */
public class UserManager {

    private final UserFileStorage storage;
    private List<User> users;

    private static UserManager instance;

    /** Creates the single instance for this execution with its injected repository; fails if it already exists. */
    public static synchronized UserManager initialize(UserFileStorage storage) {
        if (instance != null) {
            throw new IllegalStateException("UserManager is already initialized");
        }
        instance = new UserManager(storage);
        return instance;
    }

    /** The single instance of this execution; fails if not initialized. */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserManager is not initialized");
        }
        return instance;
    }

    /** Ends the current execution's instance so a new one can be initialized. */
    public static synchronized void reset() {
        instance = null;
    }

    private UserManager(UserFileStorage storage) {
        this.storage = storage;
        this.users = new ArrayList<>(storage.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
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
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public void create(String name, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireValid(UserValidator.isNonBlank(name), "invalid first name");
        requireValid(UserValidator.isNonBlank(surnames), "invalid surnames");
        requireValid(UserValidator.isValidDni(dni), "invalid DNI");
        requireValid(UserValidator.isValidEmail(email), "invalid email");
        requireValid(UserValidator.isValidPassword(password), "invalid password");
        requireValid(UserValidator.isNonBlank(phone), "invalid phone");
        requireValid(UserValidator.isValidCard(card), "invalid card number");
        requireValid(findByDni(dni) == null, "a user with this DNI already exists");
        requireValid(findByEmail(email) == null, "a user with this email already exists");

        List<User> next = new ArrayList<>(users);
        next.add(new User(name, surnames, dni, email, password, phone, card));
        commit(next);
    }

    /** Deletes the authenticated user and returns the removed object. */
    public User delete(String email, String password) {
        User user = authenticate(email, password);
        List<User> next = new ArrayList<>(users);
        next.remove(user);
        commit(next);
        return user;
    }

    public void update(String email, String password, UserField field, String value) {
        User current = authenticate(email, password);
        User changed = new User(current);
        switch (field) {
            case NAME -> {
                requireValid(UserValidator.isNonBlank(value), "invalid first name");
                changed.setName(value);
            }
            case SURNAME -> {
                requireValid(UserValidator.isNonBlank(value), "invalid surnames");
                changed.setSurnames(value);
            }
            case EMAIL -> {
                requireValid(UserValidator.isValidEmail(value), "invalid email");
                User owner = findByEmail(value);
                requireValid(owner == null || owner == current, "a user with this email already exists");
                changed.setEmail(value);
            }
            case PASSWORD -> {
                requireValid(UserValidator.isValidPassword(value), "invalid password");
                changed.setPassword(value);
            }
            case PHONE -> {
                requireValid(UserValidator.isNonBlank(value), "invalid phone");
                changed.setPhone(value);
            }
            case CARD -> {
                requireValid(UserValidator.isValidCard(value), "invalid card number");
                changed.setCard(value);
            }
        }
        List<User> next = new ArrayList<>(users);
        next.set(next.indexOf(current), changed);
        storage.save(next);
        // Apply to the existing object so every holder of the reference sees the change.
        current.copyEditableFrom(changed);
    }

    /** Returns the user with that email if the password matches, otherwise fails. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("invalid email or password");
        }
        return user;
    }

    private void commit(List<User> next) {
        storage.save(next);
        users = next;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
