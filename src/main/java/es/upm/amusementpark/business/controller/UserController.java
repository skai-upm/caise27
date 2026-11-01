package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.UserValidator;
import es.upm.amusementpark.business.repository.UserRepository;

/**
 * Creates, deletes and updates users, enforcing validation and uniqueness,
 * and keeps the repository in sync after every successful change.
 */
public class UserController {

    private final UserRepository repository;
    private final List<User> users;

    public UserController(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = repository.load();
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

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) throws OperationException {
        if (name.isEmpty()) {
            throw new OperationException("Name must not be empty");
        }
        if (surname.isEmpty()) {
            throw new OperationException("Surname must not be empty");
        }
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("Invalid DNI: " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("Invalid email: " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new OperationException("Invalid card number: it must have exactly 16 digits");
        }
        if (findByDni(dni) != null) {
            throw new OperationException("A user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new OperationException("A user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        try {
            repository.save(users);
        } catch (IOException e) {
            users.remove(user);
            throw new OperationException("Could not save users: " + e.getMessage());
        }
    }

    /**
     * Removes an already authenticated user. Business rules that may forbid the
     * deletion are checked by the caller.
     */
    public void delete(User user) throws OperationException {
        int index = users.indexOf(user);
        if (index < 0) {
            throw new OperationException("User is not registered: " + user.getEmail());
        }
        users.remove(index);
        try {
            repository.save(users);
        } catch (IOException e) {
            users.add(index, user);
            throw new OperationException("Could not save users: " + e.getMessage());
        }
    }

    public void update(String email, String password, String fieldName, String newValue)
            throws OperationException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new OperationException("Field cannot be updated: " + fieldName);
        }
        validateNewValue(user, field, newValue);
        String oldValue = getValue(user, field);
        setValue(user, field, newValue);
        try {
            repository.save(users);
        } catch (IOException e) {
            setValue(user, field, oldValue);
            throw new OperationException("Could not save users: " + e.getMessage());
        }
    }

    public User authenticate(String email, String password) throws OperationException {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new OperationException("Incorrect email or password");
        }
        return user;
    }

    private void validateNewValue(User user, UserField field, String value) throws OperationException {
        switch (field) {
            case PHONE -> {
                // stored as given
            }
            case NAME, SURNAME -> {
                if (value.isEmpty()) {
                    throw new OperationException(field + " must not be empty");
                }
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new OperationException("Invalid email: " + value);
                }
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new OperationException("A user with email " + value + " already exists");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new OperationException(
                            "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new OperationException("Invalid card number: it must have exactly 16 digits");
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
