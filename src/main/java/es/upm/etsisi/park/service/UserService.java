package es.upm.etsisi.park.service;

import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.model.UserField;
import es.upm.etsisi.park.persistence.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * User management operations. Keeps users in memory (keyed by email) and persists every change.
 */
public class UserService {

    private final UserRepository repository;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    public UserService(UserRepository repository) {
        this.repository = repository;
        for (User user : repository.loadAll()) {
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
            throw new UserException("A user with DNI " + dni + " is already registered");
        }
        if (usersByEmail.containsKey(email)) {
            throw new UserException("A user with email " + email + " is already registered");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        usersByEmail.put(email, user);
        persist();
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        usersByEmail.remove(user.getEmail());
        persist();
    }

    public User update(String email, String password, String fieldName, String newValue) {
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("Field " + fieldName + " is not modifiable. Allowed fields: "
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
                user.setPassword(newValue);
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
            throw new UserException("A user with email " + newEmail + " is already registered");
        }
        usersByEmail.remove(user.getEmail());
        user.setEmail(newEmail);
        usersByEmail.put(newEmail, user);
    }

    private User authenticate(String email, String password) {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new UserException("User with email " + email + " does not exist");
        }
        if (!user.passwordMatches(password)) {
            throw new UserException("Incorrect password");
        }
        return user;
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
            throw new UserException(label + " must not be empty");
        }
    }

    private static void requireValidDni(String dni) {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI: " + dni);
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException(
                    "Invalid password: it must contain an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number: it must contain exactly 16 digits");
        }
    }
}
