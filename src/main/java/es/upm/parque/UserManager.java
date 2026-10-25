package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for users: validation, uniqueness, create, delete and update.
 * Rejected operations throw {@link IllegalArgumentException} with the reason.
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
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
            if (u.getDni().equalsIgnoreCase(dni)) {
                return u;
            }
        }
        return null;
    }

    public User create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        requireNotEmpty(name, "name");
        requireNotEmpty(surname, "surname");
        requireNotEmpty(phone, "phone");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("Invalid DNI: " + dni);
        }
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        if (findByDni(dni) != null) {
            throw new IllegalArgumentException("DNI already registered: " + dni);
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = new User(name, surname, dni.toUpperCase(), email, password, phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    /**
     * Removes an already authenticated user. Rules involving other entities are checked by the caller.
     */
    public void remove(User user) {
        users.remove(user);
        repository.save(users);
    }

    public void update(String email, String password, String fieldText, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromText(fieldText);
        requireNotEmpty(newValue, field.name());
        switch (field) {
            case NAME -> user.setName(newValue);
            case SURNAME -> user.setSurname(newValue);
            case EMAIL -> {
                validateEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new IllegalArgumentException("Email already registered: " + newValue);
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                validatePassword(newValue);
                user.setPassword(newValue);
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                validateCard(newValue);
                user.setCard(newValue);
            }
        }
        repository.save(users);
    }

    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("Incorrect password");
        }
        return user;
    }

    private static void requireNotEmpty(String value, String fieldName) {
        if (!UserValidator.isNotEmpty(value)) {
            throw new IllegalArgumentException("Empty value for " + fieldName);
        }
    }

    private static void validateEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    private static void validatePassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void validateCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("Invalid card number: it must have exactly 16 digits");
        }
    }
}
