package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User operations: creation, deletion and update with validation, uniqueness and persistence.
 * Every method returns a message starting with "OK:" or "ERROR:"; failed operations leave state unchanged.
 */
public class UserService {

    private final UserRepository repository;
    private final List<User> users;

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public String create(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        if (!UserValidator.isNotBlank(name)) {
            return "ERROR: name must not be empty";
        }
        if (!UserValidator.isNotBlank(surname)) {
            return "ERROR: surname must not be empty";
        }
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR: invalid DNI " + dni;
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR: invalid email " + email;
        }
        if (!UserValidator.isValidPassword(password)) {
            return "ERROR: password must contain an uppercase letter, a lowercase letter, a digit and a symbol";
        }
        if (!UserValidator.isNotBlank(phone)) {
            return "ERROR: telephone must not be empty";
        }
        if (!UserValidator.isValidCard(card)) {
            return "ERROR: card number must contain exactly 16 digits";
        }
        if (findByDni(dni) != null) {
            return "ERROR: a user with DNI " + dni + " already exists";
        }
        if (findByEmail(email) != null) {
            return "ERROR: a user with email " + email + " already exists";
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return "ERROR: could not save users";
        }
        return "OK: user " + email + " created";
    }

    public String delete(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR: invalid email or password";
        }
        int index = users.indexOf(user);
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return "ERROR: could not save users";
        }
        return "OK: user " + email + " deleted";
    }

    public String update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR: invalid email or password";
        }
        UserField field;
        try {
            field = UserField.valueOf(fieldName.trim());
        } catch (IllegalArgumentException e) {
            return "ERROR: field " + fieldName + " cannot be updated (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)";
        }
        String error = validateNewValue(user, field, newValue);
        if (error != null) {
            return error;
        }
        String oldValue = apply(user, field, newValue);
        if (!persist()) {
            apply(user, field, oldValue);
            return "ERROR: could not save users";
        }
        return "OK: user " + user.getEmail() + " updated (" + field + ")";
    }

    private String validateNewValue(User user, UserField field, String value) {
        switch (field) {
            case NAME:
            case SURNAME:
            case PHONE:
                return UserValidator.isNotBlank(value) ? null : "ERROR: " + field + " must not be empty";
            case EMAIL:
                if (!UserValidator.isValidEmail(value)) {
                    return "ERROR: invalid email " + value;
                }
                User other = findByEmail(value);
                return other == null || other == user ? null : "ERROR: a user with email " + value + " already exists";
            case PASSWORD:
                return UserValidator.isValidPassword(value) ? null
                        : "ERROR: password must contain an uppercase letter, a lowercase letter, a digit and a symbol";
            case CARD:
                return UserValidator.isValidCard(value) ? null : "ERROR: card number must contain exactly 16 digits";
            default:
                return "ERROR: unsupported field " + field;
        }
    }

    /** Sets the field and returns its previous value. */
    private String apply(User user, UserField field, String value) {
        String old;
        switch (field) {
            case NAME:
                old = user.getName();
                user.setName(value);
                break;
            case SURNAME:
                old = user.getSurname();
                user.setSurname(value);
                break;
            case EMAIL:
                old = user.getEmail();
                user.setEmail(value);
                break;
            case PASSWORD:
                old = user.getPassword();
                user.setPassword(value);
                break;
            case PHONE:
                old = user.getPhone();
                user.setPhone(value);
                break;
            case CARD:
                old = user.getCard();
                user.setCard(value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported field " + field);
        }
        return old;
    }

    /** Returns the user with that email and password, or {@code null}. */
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
            if (u.getDni().equals(dni)) {
                return u;
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
