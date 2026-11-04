package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Business rules for users: creation, authenticated deletion and update,
 * uniqueness of DNI and email. Every successful change is saved through the
 * repository; if saving fails the in-memory state is restored.
 */
public class UserManager {

    private final UserFileRepository repository;
    private final List<User> users;

    public UserManager(UserFileRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) throws UserException {
        requireNotBlank(name, "first name");
        requireNotBlank(surname, "surname");
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI: " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "phone");
        requireValidCard(card);
        if (users.stream().anyMatch(u -> u.getDni().equals(dni))) {
            throw new UserException("A user with DNI " + dni + " already exists");
        }
        if (findByEmail(email).isPresent()) {
            throw new UserException("A user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        try {
            repository.save(users);
        } catch (IOException e) {
            users.remove(user);
            throw new UserException("Could not save users: " + e.getMessage());
        }
    }

    public void delete(String email, String password) throws UserException {
        User user = authenticate(email, password);
        int index = users.indexOf(user);
        users.remove(index);
        try {
            repository.save(users);
        } catch (IOException e) {
            users.add(index, user);
            throw new UserException("Could not save users: " + e.getMessage());
        }
    }

    public void update(String email, String password, UserField field, String newValue)
            throws UserException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> requireNotBlank(newValue, "first name");
            case SURNAME -> requireNotBlank(newValue, "surname");
            case PHONE -> requireNotBlank(newValue, "phone");
            case PASSWORD -> requireValidPassword(newValue);
            case CARD -> requireValidCard(newValue);
            case EMAIL -> {
                requireValidEmail(newValue);
                Optional<User> owner = findByEmail(newValue);
                if (owner.isPresent() && owner.get() != user) {
                    throw new UserException("A user with email " + newValue + " already exists");
                }
            }
        }
        User previous = new User(user);
        apply(user, field, newValue);
        try {
            repository.save(users);
        } catch (IOException e) {
            users.set(users.indexOf(user), previous);
            throw new UserException("Could not save users: " + e.getMessage());
        }
    }

    private static void apply(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setName(value);
            case SURNAME -> user.setSurname(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private User authenticate(String email, String password) throws UserException {
        Optional<User> user = findByEmail(email);
        if (user.isEmpty() || !user.get().getPassword().equals(password)) {
            throw new UserException("Incorrect email or password");
        }
        return user.get();
    }

    private static void requireNotBlank(String value, String fieldName) throws UserException {
        if (!UserValidator.isNotBlank(value)) {
            throw new UserException("The " + fieldName + " must not be empty");
        }
    }

    private static void requireValidEmail(String email) throws UserException {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) throws UserException {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) throws UserException {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("Invalid card number: it must have exactly 16 digits");
        }
    }
}
