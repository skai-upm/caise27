package com.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User operations: validation, uniqueness, authentication and persistence after each change.
 * Every method returns a one-line result message starting with "OK:" or "ERROR:".
 */
public class UserManager {

    private static final String SAVE_FAILED = "ERROR: Could not save users file.";
    private static final String BAD_CREDENTIALS = "ERROR: Unknown email or wrong password.";
    private static final String BAD_PASSWORD =
            "ERROR: Password must contain an uppercase letter, a lowercase letter, a digit and a symbol.";
    private static final String BAD_CARD = "ERROR: Card number must contain exactly 16 digits.";
    private static final String BAD_EMAIL = "ERROR: Invalid email: ";
    private static final String DUPLICATE_EMAIL = "ERROR: A user with email %s already exists.";

    private final UserFileStorage storage;
    private final List<User> users;

    public UserManager(UserFileStorage storage) throws IOException {
        this.storage = storage;
        this.users = storage.load();
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public String create(String name, String surname, String dni, String email,
                         String password, String phone, String card) {
        if (!UserValidator.isNotBlank(name)) {
            return "ERROR: First name must not be empty.";
        }
        if (!UserValidator.isNotBlank(surname)) {
            return "ERROR: Surname must not be empty.";
        }
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR: Invalid DNI: " + dni;
        }
        if (!UserValidator.isValidEmail(email)) {
            return BAD_EMAIL + email;
        }
        if (!UserValidator.isValidPassword(password)) {
            return BAD_PASSWORD;
        }
        if (!UserValidator.isNotBlank(phone)) {
            return "ERROR: Telephone must not be empty.";
        }
        if (!UserValidator.isValidCard(card)) {
            return BAD_CARD;
        }
        if (findByDni(dni) != null) {
            return "ERROR: A user with DNI " + dni + " already exists.";
        }
        if (findByEmail(email) != null) {
            return String.format(DUPLICATE_EMAIL, email);
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return SAVE_FAILED;
        }
        return "OK: User created: " + email;
    }

    public String delete(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return BAD_CREDENTIALS;
        }
        int index = users.indexOf(user);
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return SAVE_FAILED;
        }
        return "OK: User deleted: " + email;
    }

    public String update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        if (user == null) {
            return BAD_CREDENTIALS;
        }
        UserField field = UserField.fromText(fieldName);
        if (field == null) {
            return "ERROR: Field cannot be updated: " + fieldName;
        }
        String problem = validateNewValue(user, field, newValue);
        if (problem != null) {
            return problem;
        }
        User before = user.copy();
        apply(user, field, newValue);
        if (!persist()) {
            users.set(users.indexOf(user), before);
            return SAVE_FAILED;
        }
        return "OK: User updated: " + field;
    }

    private String validateNewValue(User user, UserField field, String value) {
        return switch (field) {
            case NAME, SURNAME, PHONE ->
                    UserValidator.isNotBlank(value) ? null : "ERROR: " + field + " must not be empty.";
            case EMAIL -> validateNewEmail(user, value);
            case PASSWORD -> UserValidator.isValidPassword(value) ? null : BAD_PASSWORD;
            case CARD -> UserValidator.isValidCard(value) ? null : BAD_CARD;
        };
    }

    private String validateNewEmail(User user, String value) {
        if (!UserValidator.isValidEmail(value)) {
            return BAD_EMAIL + value;
        }
        User owner = findByEmail(value);
        if (owner != null && owner != user) {
            return String.format(DUPLICATE_EMAIL, value);
        }
        return null;
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

    /** Returns the user with this email and password, or {@code null}. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
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
            if (u.getDni().equalsIgnoreCase(dni)) {
                return u;
            }
        }
        return null;
    }

    private boolean persist() {
        try {
            storage.save(users);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
