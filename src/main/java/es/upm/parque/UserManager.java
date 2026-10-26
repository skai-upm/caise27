package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for creating, deleting and updating users. Every successful change is saved.
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.loadAll());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
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

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        requireNotBlank(name, "first name");
        requireNotBlank(surname, "surname");
        requireNotBlank(phone, "telephone number");
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI: " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email: " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number: it must have exactly 16 digits");
        }
        if (findByDni(dni) != null) {
            throw new UserException("A user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserException("A user with email " + email + " already exists");
        }
        users.add(new User(name, surname, dni, email, password, phone, card));
        save();
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        users.remove(user);
        save();
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("Invalid field: " + fieldName);
        }
        requireNotBlank(newValue, "new value");
        switch (field) {
            case NAME -> user.setName(newValue);
            case SURNAME -> user.setSurname(newValue);
            case PHONE -> user.setPhone(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new UserException("Invalid email: " + newValue);
                }
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new UserException("A user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new UserException("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
                user.setPassword(newValue);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new UserException("Invalid card number: it must have exactly 16 digits");
                }
                user.setCard(newValue);
            }
        }
        save();
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("No user with email " + email);
        }
        if (!user.getPassword().equals(password)) {
            throw new UserException("Incorrect password");
        }
        return user;
    }

    private static void requireNotBlank(String value, String description) {
        if (value == null || value.isBlank()) {
            throw new UserException("The " + description + " cannot be empty");
        }
    }

    private void save() {
        try {
            repository.saveAll(users);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save users", e);
        }
    }
}
