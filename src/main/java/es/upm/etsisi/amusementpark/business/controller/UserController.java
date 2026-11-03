package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * User operations: creation with validation and uniqueness, and authenticated update and deletion.
 * Keeps the users in memory and asks the storage to persist them after every successful change.
 * Rule violations are reported with {@link IllegalArgumentException}.
 */
public class UserController {

    private final UserFileStorage storage;
    private final List<User> users;

    public UserController(UserFileStorage storage) {
        this.storage = storage;
        this.users = new ArrayList<>(storage.load());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    public User create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        require(UserValidator.isNotBlank(name), "Name must not be empty");
        require(UserValidator.isNotBlank(surname), "Surname must not be empty");
        require(UserValidator.isValidDni(dni), "Invalid DNI: " + dni);
        require(UserValidator.isValidEmail(email), "Invalid email: " + email);
        require(UserValidator.isValidPassword(password),
                "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        require(UserValidator.isNotBlank(phone), "Phone must not be empty");
        require(UserValidator.isValidCard(card), "Invalid card number: it must have exactly 16 digits");
        require(users.stream().noneMatch(u -> u.getDni().equals(dni)), "A user with DNI " + dni + " already exists");
        require(findByEmail(email).isEmpty(), "A user with email " + email + " already exists");

        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        try {
            storage.save(users);
        } catch (RuntimeException e) {
            users.remove(user);
            throw e;
        }
        return user;
    }

    /** Deletes the authenticated user and returns the removed user object. */
    public User delete(String email, String password) {
        User user = authenticate(email, password);
        int index = users.indexOf(user);
        users.remove(index);
        try {
            storage.save(users);
        } catch (RuntimeException e) {
            users.add(index, user);
            throw e;
        }
        return user;
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME, SURNAME, PHONE -> require(UserValidator.isNotBlank(newValue), field + " must not be empty");
            case EMAIL -> {
                require(UserValidator.isValidEmail(newValue), "Invalid email: " + newValue);
                Optional<User> owner = findByEmail(newValue);
                require(owner.isEmpty() || owner.get() == user, "A user with email " + newValue + " already exists");
            }
            case PASSWORD -> require(UserValidator.isValidPassword(newValue),
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
            case CARD -> require(UserValidator.isValidCard(newValue),
                    "Invalid card number: it must have exactly 16 digits");
        }
        String previous = user.get(field);
        user.set(field, newValue);
        try {
            storage.save(users);
        } catch (RuntimeException e) {
            user.set(field, previous);
            throw e;
        }
    }

    /** Returns the user with that email if the password matches; otherwise throws. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user with email " + email));
        require(user.getPassword().equals(password), "Incorrect password");
        return user;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
