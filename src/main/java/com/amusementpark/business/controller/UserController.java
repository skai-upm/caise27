package com.amusementpark.business.controller;

import com.amusementpark.business.model.User;
import com.amusementpark.business.model.UserField;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.security.PasswordHasher;
import com.amusementpark.business.validation.Validator;
import com.amusementpark.persistence.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the user-management rules (validation, uniqueness, credentials) and keeps the
 * repository in sync after every successful change. Passwords are stored only as hashes.
 */
public final class UserController {
    private static final UserController INSTANCE = new UserController();

    private UserRepository repository;
    private PasswordHasher hasher;
    private TicketController ticketController;
    private GroupController groupController;
    private final List<User> users = new ArrayList<>();

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository repository, PasswordHasher hasher, TicketController ticketController,
                          GroupController groupController) {
        this.repository = repository;
        this.hasher = hasher;
        this.ticketController = ticketController;
        this.groupController = groupController;
        users.clear();
    }

    /**
     * Loads the stored users, replacing any legacy plain-text password by its hash.
     */
    public void load() throws IOException {
        users.clear();
        users.addAll(repository.load());
        boolean migrated = false;
        for (User user : users) {
            if (!hasher.isHash(user.getPassword())) {
                user.setPassword(hasher.hash(user.getPassword()));
                migrated = true;
            }
        }
        if (migrated) {
            repository.save(users);
        }
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
        User user = new User(name, surname, dni, email, hasher.hash(password), phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return "ERROR: could not save users";
        }
        return "OK: user created";
    }

    /**
     * Deletes a user unless they hold a ticket dated on or after the system date; the user
     * is also removed from every group.
     */
    public String delete(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR: unknown email or wrong password";
        }
        IndividualTicket active = ticketController.findActiveTicket(user);
        if (active != null) {
            return "ERROR: user has a ticket dated on or after "
                    + ParkController.getInstance().getCurrentDate() + " (" + active.getId() + ")";
        }
        int index = users.indexOf(user);
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return "ERROR: could not save users";
        }
        if (!groupController.removeFromAllGroups(user)) {
            return "ERROR: user deleted but groups could not be saved";
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
        set(user, field, field == UserField.PASSWORD ? hasher.hash(value) : value);
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

    /**
     * Returns the user with that email and password, or null.
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !hasher.matches(password, user.getPassword())) {
            return null;
        }
        return user;
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
