package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.AuthenticationException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.UserField;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Singleton managing registered users (keyed by email). Passwords are validated in plain text
 * and stored only as hashes produced by the injected {@link PasswordHasher}.
 */
public final class UserController {

    private static UserController instance;

    private UserRepository repository;
    private PasswordHasher hasher;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    private UserController() {
    }

    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public void initialize(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.repository = userRepository;
        this.hasher = passwordHasher;
        usersByEmail.clear();
        for (User user : userRepository.loadAll()) {
            usersByEmail.put(user.getEmail(), user);
        }
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNotBlank(firstName, "First name");
        requireNotBlank(surnames, "Surnames");
        requireValidDni(dni);
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "Phone");
        requireValidCard(card);
        if (findByDni(dni).isPresent()) {
            throw new DuplicateException("A user with DNI " + dni + " is already registered");
        }
        if (usersByEmail.containsKey(email)) {
            throw new DuplicateException("A user with email " + email + " is already registered");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        usersByEmail.put(email, user);
        persist();
        return user;
    }

    /**
     * Removes an already authenticated user and persists the change.
     */
    public void remove(User user) {
        usersByEmail.remove(user.getEmail());
        persist();
    }

    public User update(String email, String password, String fieldName, String newValue) {
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("Field " + fieldName + " is not modifiable. Allowed fields: "
                    + allowedFields());
        }
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(newValue, "First name");
                user.setFirstName(newValue);
            }
            case SURNAME -> {
                requireNotBlank(newValue, "Surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> changeEmail(user, newValue);
            case PASSWORD -> {
                requireValidPassword(newValue);
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> {
                requireNotBlank(newValue, "Phone");
                user.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                user.setCard(newValue);
            }
        }
        persist();
        return user;
    }

    /**
     * Returns the user with the given email after checking the password against the stored hash.
     */
    public User authenticate(String email, String password) {
        User user = requireByEmail(email);
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password");
        }
        return user;
    }

    public User requireByEmail(String email) {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new NotFoundException("User with email " + email + " does not exist");
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    public Optional<User> findByDni(String dni) {
        for (User user : usersByEmail.values()) {
            if (user.getDni().equals(dni)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByEmail.values());
    }

    private void changeEmail(User user, String newEmail) {
        requireValidEmail(newEmail);
        if (newEmail.equals(user.getEmail())) {
            return;
        }
        if (usersByEmail.containsKey(newEmail)) {
            throw new DuplicateException("A user with email " + newEmail + " is already registered");
        }
        usersByEmail.remove(user.getEmail());
        user.setEmail(newEmail);
        usersByEmail.put(newEmail, user);
    }

    private void persist() {
        repository.saveAll(usersByEmail.values());
    }

    private static String allowedFields() {
        StringBuilder sb = new StringBuilder();
        for (UserField field : UserField.values()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(field.name());
        }
        return sb.toString();
    }

    private static void requireNotBlank(String value, String label) {
        if (!UserValidator.isNotBlank(value)) {
            throw new ValidationException(label + " must not be empty");
        }
    }

    private static void requireValidDni(String dni) {
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("Invalid DNI: " + dni);
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException(
                    "Invalid password: it must contain an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("Invalid card number: it must contain exactly 16 digits");
        }
    }
}
