package es.upm.etsisi.park;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User management operations. Every operation validates first and only then
 * changes state; if persisting fails the in-memory change is undone.
 */
public class UserService {

    private final UserRepository repository;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        for (User u : repository.load()) {
            usersByEmail.put(u.getEmail(), u);
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ValidationException {
        validateDni(dni);
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        for (User u : usersByEmail.values()) {
            if (u.getDni().equals(dni)) {
                throw new ValidationException("a user with DNI " + dni + " already exists");
            }
        }
        if (usersByEmail.containsKey(email)) {
            throw new ValidationException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        usersByEmail.put(email, user);
        try {
            persist();
        } catch (ValidationException e) {
            usersByEmail.remove(email);
            throw e;
        }
    }

    public void delete(String email, String password) throws ValidationException {
        User user = authenticate(email, password);
        Map<String, User> backup = new LinkedHashMap<>(usersByEmail);
        usersByEmail.remove(user.getEmail());
        try {
            persist();
        } catch (ValidationException e) {
            restore(backup);
            throw e;
        }
    }

    public void update(String email, String password, String fieldName, String value)
            throws ValidationException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("field " + fieldName + " cannot be modified");
        }
        validateNewValue(user, field, value);

        Map<String, User> backup = new LinkedHashMap<>(usersByEmail);
        User original = new User(user);
        applyField(user, field, value);
        if (field == UserField.EMAIL) {
            rekey(email, user);
        }
        try {
            persist();
        } catch (ValidationException e) {
            applyAll(user, original);
            restore(backup);
            throw e;
        }
    }

    private void validateNewValue(User user, UserField field, String value) throws ValidationException {
        switch (field) {
            case EMAIL -> {
                validateEmail(value);
                User other = usersByEmail.get(value);
                if (other != null && other != user) {
                    throw new ValidationException("a user with email " + value + " already exists");
                }
            }
            case PASSWORD -> validatePassword(value);
            case CARD -> validateCard(value);
            default -> {
                // NAME, SURNAME and PHONE only need to be stored.
            }
        }
    }

    private static void applyField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private static void applyAll(User target, User source) {
        target.setFirstName(source.getFirstName());
        target.setSurnames(source.getSurnames());
        target.setEmail(source.getEmail());
        target.setPassword(source.getPassword());
        target.setPhone(source.getPhone());
        target.setCard(source.getCard());
    }

    /** Re-inserts the user under its new email, keeping the original order. */
    private void rekey(String oldEmail, User user) {
        Map<String, User> reordered = new LinkedHashMap<>();
        for (Map.Entry<String, User> e : usersByEmail.entrySet()) {
            if (e.getKey().equals(oldEmail)) {
                reordered.put(user.getEmail(), user);
            } else {
                reordered.put(e.getKey(), e.getValue());
            }
        }
        restore(reordered);
    }

    private void restore(Map<String, User> snapshot) {
        usersByEmail.clear();
        usersByEmail.putAll(snapshot);
    }

    private User authenticate(String email, String password) throws ValidationException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new ValidationException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new ValidationException("incorrect password");
        }
        return user;
    }

    private void persist() throws ValidationException {
        try {
            repository.saveAll(usersByEmail.values());
        } catch (IOException e) {
            throw new ValidationException("could not save users: " + e.getMessage());
        }
    }

    private static void validateDni(String dni) throws ValidationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("invalid DNI " + dni);
        }
    }

    private static void validateEmail(String email) throws ValidationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("invalid email " + email);
        }
    }

    private static void validatePassword(String password) throws ValidationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException(
                    "invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void validateCard(String card) throws ValidationException {
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("invalid card number: it must have exactly 16 digits");
        }
    }
}
