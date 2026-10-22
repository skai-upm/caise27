package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.validation.UserValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User management operations. Keeps users in memory, enforces validation and
 * uniqueness, and persists every change through a {@link UserRepository}.
 * In-memory state only changes after the repository has saved successfully.
 */
public class UserService {

    private final UserRepository repository;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        for (User user : repository.loadAll()) {
            usersByEmail.put(key(user.getEmail()), user);
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }

    public User findByEmail(String email) {
        return email == null ? null : usersByEmail.get(key(email));
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws UserException {
        requireNonBlank(firstName, "First name");
        requireNonBlank(surnames, "Surnames");
        requireValidDni(dni);
        requireValidEmail(email);
        requireValidPassword(password);
        requireNonBlank(phone, "Phone");
        requireValidCard(card);

        String normalizedDni = dni.toUpperCase(Locale.ROOT);
        for (User existing : usersByEmail.values()) {
            if (existing.getDni().equalsIgnoreCase(normalizedDni)) {
                throw new UserException("A user with DNI " + normalizedDni + " already exists");
            }
        }
        if (usersByEmail.containsKey(key(email))) {
            throw new UserException("A user with email " + email + " already exists");
        }

        User user = new User(firstName, surnames, normalizedDni, email, password, phone, card);
        Map<String, User> updated = new LinkedHashMap<>(usersByEmail);
        updated.put(key(email), user);
        commit(updated);
    }

    public void delete(String email, String password) throws UserException {
        authenticate(email, password);
        Map<String, User> updated = new LinkedHashMap<>(usersByEmail);
        updated.remove(key(email));
        commit(updated);
    }

    /**
     * Changes one field of the user identified by {@code email} and
     * {@code password}. The new value must satisfy the same rules as on creation.
     */
    public void update(String email, String password, UserField field, String newValue) throws UserException {
        User current = authenticate(email, password);
        User changed = new User(current);
        switch (field) {
            case NAME -> {
                requireNonBlank(newValue, "First name");
                changed.setFirstName(newValue);
            }
            case SURNAME -> {
                requireNonBlank(newValue, "Surnames");
                changed.setSurnames(newValue);
            }
            case EMAIL -> {
                requireValidEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != current) {
                    throw new UserException("A user with email " + newValue + " already exists");
                }
                changed.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValidPassword(newValue);
                changed.setPassword(newValue);
            }
            case PHONE -> {
                requireNonBlank(newValue, "Phone");
                changed.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                changed.setCard(newValue);
            }
        }

        Map<String, User> updated = new LinkedHashMap<>();
        for (Map.Entry<String, User> entry : usersByEmail.entrySet()) {
            if (entry.getValue() == current) {
                updated.put(key(changed.getEmail()), changed);
            } else {
                updated.put(entry.getKey(), entry.getValue());
            }
        }
        commit(updated);
    }

    private User authenticate(String email, String password) throws UserException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("No user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new UserException("Incorrect password for " + email);
        }
        return user;
    }

    private void commit(Map<String, User> updated) throws UserException {
        try {
            repository.saveAll(updated.values());
        } catch (IOException e) {
            throw new UserException("Could not save users: " + e.getMessage(), e);
        }
        usersByEmail.clear();
        usersByEmail.putAll(updated);
    }

    private static String key(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private static void requireNonBlank(String value, String label) throws UserException {
        if (!UserValidator.isNonBlank(value)) {
            throw new UserException(label + " must not be empty");
        }
    }

    private static void requireValidDni(String dni) throws UserException {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI: " + dni + " (expected 8 digits and the correct check letter)");
        }
    }

    private static void requireValidEmail(String email) throws UserException {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email: " + email + " (expected text@text.xxx)");
        }
    }

    private static void requireValidPassword(String password) throws UserException {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException(
                    "Invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
        }
    }

    private static void requireValidCard(String card) throws UserException {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number: " + card + " (expected exactly 16 digits)");
        }
    }
}
