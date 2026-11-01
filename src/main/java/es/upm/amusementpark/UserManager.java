package es.upm.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the registered users, enforces validation and uniqueness rules, and
 * persists every successful change through a {@link UserRepository}.
 * Rejected operations leave both memory and storage unchanged.
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
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

    private User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        requireNotEmpty(name, "name");
        requireNotEmpty(surname, "surname");
        requireNotEmpty(phone, "telephone");
        requireValid(UserField.EMAIL, email);
        requireValid(UserField.PASSWORD, password);
        requireValid(UserField.CARD, card);
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("invalid DNI: " + dni);
        }
        if (findByDni(dni) != null) {
            throw new OperationException("a user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new OperationException("a user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        try {
            persist();
        } catch (OperationException e) {
            users.remove(user);
            throw e;
        }
    }

    /** Removes an already authenticated user and persists the change. */
    public void remove(User user) {
        int index = users.indexOf(user);
        users.remove(index);
        try {
            persist();
        } catch (OperationException e) {
            users.add(index, user);
            throw e;
        }
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        requireValid(field, newValue);
        if (field == UserField.EMAIL) {
            User owner = findByEmail(newValue);
            if (owner != null && owner != user) {
                throw new OperationException("a user with email " + newValue + " already exists");
            }
        }
        String oldValue = user.get(field);
        user.set(field, newValue);
        try {
            persist();
        } catch (OperationException e) {
            user.set(field, oldValue);
            throw e;
        }
    }

    /** Returns the user with that email if the password matches; otherwise rejects the request. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new OperationException("incorrect email or password");
        }
        return user;
    }

    private static void requireValid(UserField field, String value) {
        boolean valid = switch (field) {
            case NAME, SURNAME, PHONE -> UserValidator.isNotEmpty(value);
            case EMAIL -> UserValidator.isValidEmail(value);
            case PASSWORD -> UserValidator.isValidPassword(value);
            case CARD -> UserValidator.isValidCard(value);
        };
        if (!valid) {
            throw new OperationException("invalid " + field.name().toLowerCase());
        }
    }

    private static void requireNotEmpty(String value, String label) {
        if (!UserValidator.isNotEmpty(value)) {
            throw new OperationException(label + " must not be empty");
        }
    }

    private void persist() {
        try {
            repository.save(users);
        } catch (IOException e) {
            throw new OperationException("could not save users: " + e.getMessage());
        }
    }
}
