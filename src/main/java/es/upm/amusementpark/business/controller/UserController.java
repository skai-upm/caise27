package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.AuthenticationException;
import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.UserValidator;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import java.util.List;

/**
 * User registration, deletion and modification (Singleton).
 */
public final class UserController {

    private static final String PASSWORD_RULES =
            "invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)";

    private static UserController instance;

    private Park park;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private PasswordHasher hasher;

    private UserController() {
    }

    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public void configure(Park park, UserRepository users, GroupRepository groups, TicketRepository tickets,
                          PasswordHasher hasher) {
        this.park = park;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.hasher = hasher;
    }

    public User create(String firstName, String surnames, String dni, String email, String password, String phone,
                       String card) {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("invalid DNI " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException(PASSWORD_RULES);
        }
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("invalid card number (must be exactly 16 digits)");
        }
        if (users.findByDni(dni) != null) {
            throw new DuplicateException("a user with DNI " + dni + " already exists");
        }
        if (users.findByEmail(email) != null) {
            throw new DuplicateException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        users.add(user);
        return user;
    }

    /**
     * Deletes a user who holds no active ticket and belongs to no locked group. The user is removed from their
     * groups; historical tickets are kept and still refer to the former user.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        for (IndividualTicket ticket : tickets.ticketsOf(user)) {
            if (ticket.isActiveOn(park.getDate())) {
                throw new OperationNotAllowedException("user " + email + " has active ticket " + ticket.getId());
            }
        }
        List<Group> userGroups = groups.groupsOf(user);
        for (Group group : userGroups) {
            if (park.isLocked(group)) {
                throw new OperationNotAllowedException("user " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        for (Group group : userGroups) {
            group.removeMember(user);
        }
        groups.save();
        users.remove(user);
        tickets.save();
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new InvalidDataException("invalid email " + newValue);
                }
                User other = users.findByEmail(newValue);
                if (other != null && other != user) {
                    throw new DuplicateException("a user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new InvalidDataException(PASSWORD_RULES);
                }
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new InvalidDataException("invalid card number (must be exactly 16 digits)");
                }
                user.setCard(newValue);
            }
        }
        users.save();
    }

    private User authenticate(String email, String password) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("incorrect password");
        }
        return user;
    }
}
