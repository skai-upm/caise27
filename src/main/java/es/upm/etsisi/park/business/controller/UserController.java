package es.upm.etsisi.park.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.exception.AuthenticationException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.business.model.user.UserField;
import es.upm.etsisi.park.business.model.user.UserValidator;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.UserRepository;

/**
 * Manages registered users. Singleton; its repository, password hasher and
 * collaborating controllers are injected with {@link #configure}.
 * Every successful change is saved; if saving fails the change is undone.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private UserRepository repository;
    private PasswordHasher hasher;
    private TicketController tickets;
    private GroupController groups;
    private final List<User> users = new ArrayList<>();

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository repository, PasswordHasher hasher, TicketController tickets,
                          GroupController groups) {
        this.repository = repository;
        this.hasher = hasher;
        this.tickets = tickets;
        this.groups = groups;
        users.clear();
    }

    public void load() throws PersistenceException {
        users.clear();
        try {
            users.addAll(repository.load());
        } catch (IOException e) {
            throw new PersistenceException("Could not load users: " + e.getMessage(), e);
        }
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public boolean isActive(User user) {
        return users.contains(user);
    }

    public Map<String, User> usersByDni() {
        Map<String, User> byDni = new HashMap<>();
        for (User user : users) {
            byDni.put(user.getDni(), user);
        }
        return byDni;
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
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

    public User require(String email) throws NotFoundException {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User with email " + email + " not found");
        }
        return user;
    }

    public User create(String firstName, String surnames, String dni, String email, String password,
                       String phone, String card) throws ParkException {
        validate(UserField.NAME, firstName);
        validate(UserField.SURNAME, surnames);
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("Invalid DNI '" + dni + "': expected 8 digits and the matching control letter");
        }
        validate(UserField.EMAIL, email);
        validate(UserField.PASSWORD, password);
        validate(UserField.PHONE, phone);
        validate(UserField.CARD, card);
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            throw new DuplicateException("A user with DNI " + normalizedDni + " is already registered");
        }
        if (findByEmail(email) != null) {
            throw new DuplicateException("A user with email " + email + " is already registered");
        }
        User user = new User(firstName, surnames, normalizedDni, email, hasher.hash(password), phone, card);
        users.add(user);
        try {
            persist();
        } catch (PersistenceException e) {
            users.remove(user);
            throw e;
        }
        return user;
    }

    /** Returns the user with that email if the password matches the stored hash. */
    public User authenticate(String email, String password) throws ParkException {
        User user = require(email);
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password for " + email);
        }
        return user;
    }

    public void update(String email, String password, UserField field, String newValue) throws ParkException {
        User user = authenticate(email, password);
        validate(field, newValue);
        if (field == UserField.EMAIL) {
            User other = findByEmail(newValue);
            if (other != null && other != user) {
                throw new DuplicateException("A user with email " + newValue + " is already registered");
            }
        }
        String oldValue = user.get(field);
        user.set(field, field == UserField.PASSWORD ? hasher.hash(newValue) : newValue);
        try {
            persist();
        } catch (PersistenceException e) {
            user.set(field, oldValue);
            throw e;
        }
        tickets.persist();
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group;
     * the user is removed from their groups and historical tickets keep them.
     */
    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (tickets.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("User " + email + " has an active ticket and cannot be deleted");
        }
        for (Group group : groups.groupsOf(user)) {
            if (groups.isLocked(group)) {
                throw new OperationNotAllowedException("User " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        int index = users.indexOf(user);
        users.remove(index);
        try {
            persist();
        } catch (PersistenceException e) {
            users.add(index, user);
            throw e;
        }
        groups.removeFromAllGroups(user);
        tickets.persist();
    }

    private void validate(UserField field, String value) throws ValidationException {
        switch (field) {
            case NAME -> requireNonEmpty(value, "First name");
            case SURNAME -> requireNonEmpty(value, "Surnames");
            case PHONE -> requireNonEmpty(value, "Phone");
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new ValidationException("Invalid email '" + value + "': expected text@text.xxx");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new ValidationException(
                            "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new ValidationException("Invalid card number '" + value + "': expected exactly 16 digits");
                }
            }
        }
    }

    private static void requireNonEmpty(String value, String label) throws ValidationException {
        if (!UserValidator.isNonEmpty(value)) {
            throw new ValidationException(label + " must not be empty");
        }
    }

    private void persist() throws PersistenceException {
        try {
            repository.save(users);
        } catch (IOException e) {
            throw new PersistenceException("Could not save users: " + e.getMessage(), e);
        }
    }
}
