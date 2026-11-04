package com.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User operations: creation, authenticated deletion and update, uniqueness of DNI and email.
 * Every rejected operation throws {@link IllegalArgumentException} and leaves memory and file unchanged.
 */
public class UserService {
    private final UserRepository repository;
    private List<User> users;

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
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

    public void create(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        require(UserValidator.isNonBlank(name), "First name must not be empty");
        require(UserValidator.isNonBlank(surname), "Surname must not be empty");
        require(UserValidator.isValidDni(dni), "Invalid DNI: " + dni);
        require(UserValidator.isValidEmail(email), "Invalid email: " + email);
        require(UserValidator.isValidPassword(password),
                "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        require(UserValidator.isNonBlank(phone), "Phone must not be empty");
        require(UserValidator.isValidCard(card), "Invalid card number: it must have exactly 16 digits");
        require(findByDni(dni) == null, "A user with DNI " + dni + " already exists");
        require(findByEmail(email) == null, "A user with email " + email + " already exists");

        List<User> next = new ArrayList<>(users);
        next.add(new User(name, surname, dni, email, password, phone, card));
        commit(next);
    }

    /** Deletes the authenticated user and returns the removed user object. */
    public User delete(String email, String password) {
        User user = authenticate(email, password);
        List<User> next = new ArrayList<>(users);
        next.remove(user);
        commit(next);
        return user;
    }

    public void update(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> require(UserValidator.isNonBlank(value), "First name must not be empty");
            case SURNAME -> require(UserValidator.isNonBlank(value), "Surname must not be empty");
            case PHONE -> require(UserValidator.isNonBlank(value), "Phone must not be empty");
            case PASSWORD -> require(UserValidator.isValidPassword(value),
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
            case CARD -> require(UserValidator.isValidCard(value),
                    "Invalid card number: it must have exactly 16 digits");
            case EMAIL -> {
                require(UserValidator.isValidEmail(value), "Invalid email: " + value);
                User owner = findByEmail(value);
                require(owner == null || owner == user, "A user with email " + value + " already exists");
            }
        }
        // Save the edited state first; only then change the stored object in place, so that every
        // reference to this user (e.g. from groups) sees the new value and a failed save changes nothing.
        User changed = new User(user);
        changed.set(field, value);
        List<User> toSave = new ArrayList<>(users);
        toSave.set(toSave.indexOf(user), changed);
        save(toSave);
        user.set(field, value);
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        require(user != null, "No user with email " + email);
        require(user.getPassword().equals(password), "Incorrect password");
        return user;
    }

    /** Persists the new state first; memory only changes if the file was written. */
    private void commit(List<User> next) {
        save(next);
        users = next;
    }

    private void save(List<User> state) {
        try {
            repository.save(state);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save users: " + e.getMessage(), e);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
