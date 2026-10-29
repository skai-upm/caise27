package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.AuthenticationException;
import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.repository.UserRepository;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.business.validation.UserValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages active users: creation, modification, authentication and deletion.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private final Map<String, User> usersByEmail = new LinkedHashMap<>();
    private UserRepository repository;
    private PasswordHasher hasher;
    private TicketController tickets;
    private GroupController groups;
    private AttractionController attractions;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    public void setCollaborators(TicketController tickets, GroupController groups, AttractionController attractions) {
        this.tickets = tickets;
        this.groups = groups;
        this.attractions = attractions;
    }

    public void load() throws PersistenceException {
        usersByEmail.clear();
        try {
            for (User user : repository.load()) {
                usersByEmail.put(user.getEmail(), user);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not load users: " + e.getMessage(), e);
        }
    }

    public void save() throws PersistenceException {
        try {
            repository.saveAll(usersByEmail.values());
        } catch (IOException e) {
            throw new PersistenceException("could not save users: " + e.getMessage(), e);
        }
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ParkException {
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new DuplicateException("a user with this DNI already exists");
        }
        if (usersByEmail.containsKey(email)) {
            throw new DuplicateException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        usersByEmail.put(email, user);
        return user;
    }

    public void update(String email, String password, UserField field, String value) throws ParkException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case PHONE -> user.setPhone(value);
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new ValidationException("invalid password");
                }
                user.setPasswordHash(hasher.hash(value));
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new ValidationException("invalid card number");
                }
                user.setCard(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new ValidationException("invalid email");
                }
                User owner = usersByEmail.get(value);
                if (owner != null && owner != user) {
                    throw new DuplicateException("a user with this email already exists");
                }
                usersByEmail.remove(user.getEmail());
                user.setEmail(value);
                usersByEmail.put(value, user);
            }
        }
    }

    /**
     * Deletes a user who has no active ticket and belongs to no locked group, removing them from their
     * groups and attractions. Historical tickets keep referring to the deleted user.
     */
    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        for (IndividualTicket ticket : tickets.ticketsOf(user)) {
            if (tickets.isActive(ticket)) {
                throw new OperationNotAllowedException("user has an active ticket: " + ticket.getId());
            }
        }
        List<Group> memberships = groups.groupsOf(user);
        for (Group group : memberships) {
            if (groups.isLocked(group)) {
                throw new OperationNotAllowedException("user belongs to locked group " + group.getId());
            }
        }
        for (Group group : memberships) {
            group.removeMember(user);
        }
        attractions.removeEverywhere(user);
        usersByEmail.remove(user.getEmail(), user);
    }

    /**
     * Returns the user with this email if the password matches the stored hash.
     */
    public User authenticate(String email, String password) throws ParkException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new NotFoundException("user not found");
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("incorrect password");
        }
        return user;
    }

    public User requireUser(String email) throws NotFoundException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new NotFoundException("user not found: " + email);
        }
        return user;
    }

    public boolean isActive(User user) {
        return usersByEmail.get(user.getEmail()) == user;
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public User findByDni(String dni) {
        for (User user : usersByEmail.values()) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public Map<String, User> usersByDni() {
        Map<String, User> result = new HashMap<>();
        for (User user : usersByEmail.values()) {
            result.put(user.getDni(), user);
        }
        return result;
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }
}
