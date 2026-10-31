package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.repository.UserRepository;
import es.upm.amusementpark.business.rules.UserValidator;
import es.upm.amusementpark.business.security.PasswordHasher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the registered users and applies the user-management rules. Every
 * successful change is persisted through {@link UserRepository}; a rejected
 * operation leaves both memory and file untouched.
 *
 * <p>Operations signal rejection with {@link IllegalArgumentException} (invalid
 * data or credentials) or {@link IOException} (the change could not be saved
 * and has been undone).</p>
 */
public class UserManager {

    private static final SingletonSlot<UserManager> INSTANCE = new SingletonSlot<>("UserManager");

    /** Creates the single instance for this execution with its injected dependencies. */
    public static UserManager initialize(UserRepository repository, PasswordHasher hasher) throws IOException {
        return INSTANCE.initialize(() -> new UserManager(repository, hasher));
    }

    /** Returns the instance created by {@link #initialize}. */
    public static UserManager getInstance() {
        return INSTANCE.get();
    }

    /** Discards the instance at the end of an execution. */
    public static void reset() {
        INSTANCE.reset();
    }

    private final UserRepository repository;
    private final PasswordHasher hasher;
    private final List<User> users;

    private UserManager(UserRepository repository, PasswordHasher hasher) throws IOException {
        this.repository = repository;
        this.hasher = hasher;
        this.users = repository.loadAll();
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public User create(String name, String surname, String dni, String email,
                       String password, String phone, String card) throws IOException {
        require(UserValidator.isNotBlank(name), "name must not be empty");
        require(UserValidator.isNotBlank(surname), "surname must not be empty");
        require(UserValidator.isValidDni(dni), "invalid DNI");
        require(UserValidator.isValidEmail(email), "invalid email");
        require(UserValidator.isValidPassword(password),
                "password must contain an uppercase letter, a lowercase letter, a digit and a symbol");
        require(UserValidator.isNotBlank(phone), "phone must not be empty");
        require(UserValidator.isValidCard(card), "card number must have exactly 16 digits");
        require(findByDni(dni) == null, "a user with that DNI already exists");
        require(findByEmail(email) == null, "a user with that email already exists");

        User user = new User(name, surname, dni, email, hasher.hash(password), phone, card);
        users.add(user);
        try {
            repository.saveAll(users);
        } catch (IOException e) {
            users.remove(user);
            throw e;
        }
        return user;
    }

    public void delete(String email, String password) throws IOException {
        remove(authenticate(email, password));
    }

    /** Removes an already authenticated user and persists the change. */
    public void remove(User user) throws IOException {
        int index = users.indexOf(user);
        users.remove(index);
        try {
            repository.saveAll(users);
        } catch (IOException e) {
            users.add(index, user);
            throw e;
        }
    }

    public void update(String email, String password, UserField field, String newValue)
            throws IOException {
        User user = authenticate(email, password);
        validateNewValue(user, field, newValue);
        String stored = field == UserField.PASSWORD ? hasher.hash(newValue) : newValue;
        String previous = set(user, field, stored);
        try {
            repository.saveAll(users);
        } catch (IOException e) {
            set(user, field, previous);
            throw e;
        }
    }

    /** Returns the user with that email and password, or rejects the credentials. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        require(user != null && hasher.matches(password, user.getPassword()), "invalid email or password");
        return user;
    }

    private void validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case NAME -> require(UserValidator.isNotBlank(value), "name must not be empty");
            case SURNAME -> require(UserValidator.isNotBlank(value), "surname must not be empty");
            case PHONE -> require(UserValidator.isNotBlank(value), "phone must not be empty");
            case EMAIL -> {
                require(UserValidator.isValidEmail(value), "invalid email");
                User owner = findByEmail(value);
                require(owner == null || owner == user, "a user with that email already exists");
            }
            case PASSWORD -> require(UserValidator.isValidPassword(value),
                    "password must contain an uppercase letter, a lowercase letter, a digit and a symbol");
            case CARD -> require(UserValidator.isValidCard(value), "card number must have exactly 16 digits");
        }
    }

    /** Sets the field and returns its previous value. */
    private static String set(User user, UserField field, String value) {
        String previous;
        switch (field) {
            case NAME -> {
                previous = user.getName();
                user.setName(value);
            }
            case SURNAME -> {
                previous = user.getSurname();
                user.setSurname(value);
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
            default -> throw new IllegalStateException("unsupported field " + field);
        }
        return previous;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
