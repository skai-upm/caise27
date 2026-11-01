package es.upm.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates, deletes and updates users, enforcing validation and uniqueness,
 * and keeps the storage in sync after every successful change.
 */
public class UserManager {

    private final UserStorage storage;
    private final List<User> users;

    public UserManager(UserStorage storage) throws IOException {
        this.storage = storage;
        this.users = storage.load();
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    private User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) throws UserOperationException {
        if (name.isEmpty()) {
            throw new UserOperationException("Name must not be empty");
        }
        if (surname.isEmpty()) {
            throw new UserOperationException("Surname must not be empty");
        }
        if (!UserValidator.isValidDni(dni)) {
            throw new UserOperationException("Invalid DNI: " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("Invalid email: " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserOperationException("Invalid card number: it must have exactly 16 digits");
        }
        if (findByDni(dni) != null) {
            throw new UserOperationException("A user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserOperationException("A user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        try {
            storage.save(users);
        } catch (IOException e) {
            users.remove(user);
            throw new UserOperationException("Could not save users: " + e.getMessage());
        }
    }

    public void delete(String email, String password) throws UserOperationException {
        User user = authenticate(email, password);
        int index = users.indexOf(user);
        users.remove(index);
        try {
            storage.save(users);
        } catch (IOException e) {
            users.add(index, user);
            throw new UserOperationException("Could not save users: " + e.getMessage());
        }
    }

    public void update(String email, String password, String fieldName, String newValue)
            throws UserOperationException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserOperationException("Field cannot be updated: " + fieldName);
        }
        validateNewValue(user, field, newValue);
        String oldValue = getValue(user, field);
        setValue(user, field, newValue);
        try {
            storage.save(users);
        } catch (IOException e) {
            setValue(user, field, oldValue);
            throw new UserOperationException("Could not save users: " + e.getMessage());
        }
    }

    private User authenticate(String email, String password) throws UserOperationException {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new UserOperationException("Incorrect email or password");
        }
        return user;
    }

    private void validateNewValue(User user, UserField field, String value) throws UserOperationException {
        switch (field) {
            case PHONE -> {
                // stored as given
            }
            case NAME, SURNAME -> {
                if (value.isEmpty()) {
                    throw new UserOperationException(field + " must not be empty");
                }
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserOperationException("Invalid email: " + value);
                }
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new UserOperationException("A user with email " + value + " already exists");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserOperationException(
                            "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserOperationException("Invalid card number: it must have exactly 16 digits");
                }
            }
        }
    }

    private static String getValue(User user, UserField field) {
        return switch (field) {
            case NAME -> user.getName();
            case SURNAME -> user.getSurname();
            case EMAIL -> user.getEmail();
            case PASSWORD -> user.getPassword();
            case PHONE -> user.getPhone();
            case CARD -> user.getCard();
        };
    }

    private static void setValue(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setName(value);
            case SURNAME -> user.setSurname(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }
}
