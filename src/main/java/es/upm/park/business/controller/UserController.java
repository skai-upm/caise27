package es.upm.park.business.controller;

import es.upm.park.business.exception.AuthenticationException;
import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.business.security.PasswordHasher;
import es.upm.park.business.validation.UserValidator;

import java.util.List;

/**
 * User management: creation, modification and deletion with validation,
 * uniqueness, password protection and the ticket/group restrictions on
 * deletion. Single instance; collaborators are injected with
 * {@link #configure}.
 */
public final class UserController {

    private Park park;
    private PasswordHasher hasher;

    private UserController() {
    }

    private static final class Holder {
        private static final UserController INSTANCE = new UserController();
    }

    public static UserController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park, PasswordHasher hasher) {
        this.park = park;
        this.hasher = hasher;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("UserController has not been configured");
        }
        return park;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ParkException {
        requireNotBlank(firstName, "first name");
        requireNotBlank(surnames, "surnames");
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("Invalid DNI: " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "phone");
        requireValidCard(card);
        if (park().findUserByDni(dni) != null) {
            throw new ConflictException("A user with DNI " + dni + " already exists");
        }
        if (park().findUserByEmail(email) != null) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        park().addUser(user);
        park().save();
        return user;
    }

    public void update(String email, String password, String fieldName, String newValue) throws ParkException {
        UserField field = UserField.parse(fieldName);
        if (field == null) {
            throw new ValidationException("Field " + fieldName + " cannot be modified. Modifiable fields: "
                    + UserField.listNames());
        }
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(newValue, "first name");
                user.setFirstName(newValue);
            }
            case SURNAME -> {
                requireNotBlank(newValue, "surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                requireValidEmail(newValue);
                User owner = park().findUserByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new ConflictException("A user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValidPassword(newValue);
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> {
                requireNotBlank(newValue, "phone");
                user.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                user.setCard(newValue);
            }
        }
        park().save();
    }

    /**
     * Deletes a user after checking credentials, active tickets and locked
     * groups; the user is removed from every group it belonged to.
     * Historical tickets keep referring to the user.
     */
    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (park().hasActiveTicket(user)) {
            throw new BusinessRuleException("The user " + email + " has active tickets and cannot be deleted");
        }
        List<Group> memberships = park().groupsOf(user);
        for (Group group : memberships) {
            if (park().isLocked(group)) {
                throw new BusinessRuleException("The user " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        park().removeUser(user);
        park().save();
    }

    public User authenticate(String email, String password) throws ParkException {
        User user = park().findUserByEmail(email);
        if (user == null) {
            throw new NotFoundException("No user with email " + email);
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password for " + email);
        }
        return user;
    }

    private static void requireNotBlank(String value, String fieldName) throws ValidationException {
        if (!UserValidator.isNotBlank(value)) {
            throw new ValidationException("The " + fieldName + " must not be empty");
        }
    }

    private static void requireValidEmail(String email) throws ValidationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) throws ValidationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) throws ValidationException {
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("Invalid card number: it must contain exactly 16 digits");
        }
    }
}
