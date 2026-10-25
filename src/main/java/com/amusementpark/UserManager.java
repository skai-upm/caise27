package com.amusementpark;

import java.io.IOException;
import java.util.List;

/**
 * Applies the user-management rules (validation, uniqueness, credentials) and
 * keeps the repository in sync after every successful change.
 */
public class UserManager {
    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = repository.load();
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public String create(String name, String surname, String dni, String email,
                         String password, String phone, String card) {
        if (name.isBlank() || surname.isBlank() || phone.isBlank()) {
            return "ERROR: name, surname and phone are required";
        }
        if (!Validator.isValidDni(dni)) {
            return "ERROR: invalid DNI";
        }
        if (!Validator.isValidEmail(email)) {
            return "ERROR: invalid email";
        }
        if (!Validator.isValidPassword(password)) {
            return "ERROR: invalid password";
        }
        if (!Validator.isValidCard(card)) {
            return "ERROR: invalid card number";
        }
        if (findByDni(dni) != null) {
            return "ERROR: a user with that DNI already exists";
        }
        if (findByEmail(email) != null) {
            return "ERROR: a user with that email already exists";
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return "ERROR: could not save users";
        }
        return "OK: user created";
    }

    public String delete(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR: unknown email or wrong password";
        }
        int index = users.indexOf(user);
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return "ERROR: could not save users";
        }
        return "OK: user deleted";
    }

    public String update(String email, String password, String fieldName, String value) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR: unknown email or wrong password";
        }
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            return "ERROR: field cannot be updated: " + fieldName;
        }
        String error = validateNewValue(user, field, value);
        if (error != null) {
            return error;
        }
        String previous = get(user, field);
        set(user, field, value);
        if (!persist()) {
            set(user, field, previous);
            return "ERROR: could not save users";
        }
        return "OK: user updated";
    }

    private String validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case EMAIL:
                if (!Validator.isValidEmail(value)) {
                    return "ERROR: invalid email";
                }
                User owner = findByEmail(value);
                if (owner != null && owner != user) {
                    return "ERROR: a user with that email already exists";
                }
                return null;
            case PASSWORD:
                return Validator.isValidPassword(value) ? null : "ERROR: invalid password";
            case CARD:
                return Validator.isValidCard(value) ? null : "ERROR: invalid card number";
            default:
                return value.isBlank() ? "ERROR: value cannot be empty" : null;
        }
    }

    private static String get(User user, UserField field) {
        switch (field) {
            case NAME:
                return user.getName();
            case SURNAME:
                return user.getSurname();
            case EMAIL:
                return user.getEmail();
            case PASSWORD:
                return user.getPassword();
            case PHONE:
                return user.getPhone();
            default:
                return user.getCard();
        }
    }

    private static void set(User user, UserField field, String value) {
        switch (field) {
            case NAME:
                user.setName(value);
                break;
            case SURNAME:
                user.setSurname(value);
                break;
            case EMAIL:
                user.setEmail(value);
                break;
            case PASSWORD:
                user.setPassword(value);
                break;
            case PHONE:
                user.setPhone(value);
                break;
            default:
                user.setCard(value);
                break;
        }
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    private User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equalsIgnoreCase(dni)) {
                return user;
            }
        }
        return null;
    }

    private boolean persist() {
        try {
            repository.save(users);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
