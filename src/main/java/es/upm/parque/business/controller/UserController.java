package es.upm.parque.business.controller;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.repository.UserRepository;
import es.upm.parque.business.security.PasswordEncoder;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Business rules for users: validation, uniqueness and credential checks.
 * Every successful change is saved through the repository.
 */
public class UserController {
    private static final UserController INSTANCE = new UserController();

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private final List<User> users = new ArrayList<>();
    private final List<Function<User, String>> deletionChecks = new ArrayList<>();

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and password encoder, reloads the users (hashing any legacy plaintext password) and
     * discards previously registered checks.
     */
    public UserController configure(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        users.clear();
        users.addAll(repository.load());
        deletionChecks.clear();
        boolean migrated = false;
        for (User u : users) {
            if (!passwordEncoder.isHash(u.getPassword())) {
                u.setPassword(passwordEncoder.hash(u.getPassword()));
                migrated = true;
            }
        }
        if (migrated) {
            repository.save(users);
        }
        return this;
    }

    public User create(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        requireNotBlank(name, "first name");
        requireNotBlank(surname, "surname");
        requireNotBlank(phone, "telephone number");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("invalid DNI '" + dni + "'");
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireValidCard(card);
        if (findByDni(dni) != null) {
            throw new IllegalArgumentException("a user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("a user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, passwordEncoder.hash(password), phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    /**
     * Registers a rule that can veto deleting a user: it returns the reason for refusing, or null to allow it.
     */
    public void addDeletionCheck(Function<User, String> check) {
        deletionChecks.add(check);
    }

    public User delete(String email, String password) {
        User user = authenticate(email, password);
        for (Function<User, String> check : deletionChecks) {
            String reason = check.apply(user);
            if (reason != null) {
                throw new IllegalArgumentException("user " + email + " cannot be deleted: " + reason);
            }
        }
        users.remove(user);
        repository.save(users);
        return user;
    }

    public User update(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(value, "first name");
                user.setName(value);
            }
            case SURNAME -> {
                requireNotBlank(value, "surname");
                user.setSurname(value);
            }
            case EMAIL -> {
                requireValidEmail(value);
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new IllegalArgumentException("email " + value + " belongs to another user");
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                requireValidPassword(value);
                user.setPassword(passwordEncoder.hash(value));
            }
            case PHONE -> {
                requireNotBlank(value, "telephone number");
                user.setPhone(value);
            }
            case CARD -> {
                requireValidCard(value);
                user.setCard(value);
            }
        }
        repository.save(users);
        return user;
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

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("incorrect password for " + email);
        }
        return user;
    }

    private static void requireNotBlank(String value, String what) {
        if (!UserValidator.isNotBlank(value)) {
            throw new IllegalArgumentException("the " + what + " cannot be empty");
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("invalid email '" + email + "'");
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("invalid card number '" + card + "': it must have exactly 16 digits");
        }
    }
}
