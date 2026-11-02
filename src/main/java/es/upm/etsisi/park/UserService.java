package es.upm.etsisi.park;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User management rules: validation, uniqueness, authentication and persistence.
 * Every mutation is all-or-nothing: if saving fails the in-memory state is restored.
 */
public class UserService {

    private final UserRepository repository;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    public UserService(UserRepository repository) {
        this.repository = repository;
        for (User u : repository.load()) {
            usersByEmail.put(u.getEmail(), u);
        }
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(usersByEmail.values());
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNotBlank(firstName, surnames, dni, email, password, phone, card);
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("Invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number");
        }
        for (User u : usersByEmail.values()) {
            if (u.getDni().equals(dni)) {
                throw new UserException("DNI already registered");
            }
        }
        if (usersByEmail.containsKey(email)) {
            throw new UserException("Email already registered");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        usersByEmail.put(email, user);
        try {
            persist();
        } catch (UserException e) {
            usersByEmail.remove(email);
            throw e;
        }
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        Map<String, User> before = new LinkedHashMap<>(usersByEmail);
        usersByEmail.remove(user.getEmail());
        try {
            persist();
        } catch (UserException e) {
            restore(before);
            throw e;
        }
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("Field cannot be modified: " + fieldName);
        }
        requireNotBlank(newValue);
        validateNewValue(user, field, newValue);

        Map<String, User> before = new LinkedHashMap<>(usersByEmail);
        String previous = apply(user, field, newValue);
        if (field == UserField.EMAIL) {
            usersByEmail.remove(email);
            usersByEmail.put(newValue, user);
        }
        try {
            persist();
        } catch (UserException e) {
            apply(user, field, previous);
            restore(before);
            throw e;
        }
    }

    private void validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserException("Invalid email");
                }
                User other = usersByEmail.get(value);
                if (other != null && other != user) {
                    throw new UserException("Email already registered");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserException("Invalid password");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserException("Invalid card number");
                }
            }
            default -> {
                // NAME, SURNAME and PHONE have no format restriction beyond being non-blank.
            }
        }
    }

    /** Sets the field and returns its previous value. */
    private static String apply(User user, UserField field, String value) {
        String previous;
        switch (field) {
            case NAME -> {
                previous = user.getFirstName();
                user.setFirstName(value);
            }
            case SURNAME -> {
                previous = user.getSurnames();
                user.setSurnames(value);
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
            default -> throw new IllegalStateException("Unhandled field " + field);
        }
        return previous;
    }

    private User authenticate(String email, String password) {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new UserException("User not found");
        }
        if (!user.checkPassword(password)) {
            throw new UserException("Incorrect password");
        }
        return user;
    }

    private void persist() {
        repository.save(new ArrayList<>(usersByEmail.values()));
    }

    private void restore(Map<String, User> snapshot) {
        usersByEmail.clear();
        usersByEmail.putAll(snapshot);
    }

    private static void requireNotBlank(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) {
                throw new UserException("Malformed command: empty field");
            }
        }
    }
}
