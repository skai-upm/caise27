package es.upm.amusementpark.business.service;

import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.business.exception.UserOperationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.persistence.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * User management operations. Applies the validation and uniqueness rules
 * before changing the stored users.
 */
public class UserService {

    private static final UserService INSTANCE = new UserService();

    private UserRepository users;
    private GroupRepository groups;
    private ParkController park;
    private PasswordHasher passwordHasher;

    private UserService() {
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    /** Injects the collaborators of this controller; called during application initialization. */
    public void configure(UserRepository users, GroupRepository groups, ParkController park,
            PasswordHasher passwordHasher) {
        this.users = users;
        this.groups = groups;
        this.park = park;
        this.passwordHasher = passwordHasher;
    }

    public User createUser(String firstName, String surnames, String dni, String email,
            String password, String phone, String card) throws UserOperationException {
        List<String> errors = UserValidator.validate(dni, email, password, card);
        if (!errors.isEmpty()) {
            throw new UserOperationException(String.join(" ", errors));
        }
        if (users.existsByDni(dni)) {
            throw new UserOperationException("A user with DNI " + dni + " already exists.");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new UserOperationException("A user with email " + email + " already exists.");
        }
        User user = new User(firstName, surnames, dni, email, passwordHasher.hash(password), phone, card);
        users.save(user);
        return user;
    }

    public void deleteUser(String email, String password) throws UserOperationException {
        User user = authenticate(email, password);
        if (park.hasActiveTicket(user)) {
            throw new UserOperationException("User " + email
                    + " has a ticket for the current date or later and cannot be deleted.");
        }
        if (park.belongsToLockedGroup(user)) {
            throw new UserOperationException("User " + email
                    + " belongs to a group with a group ticket for the current date or later and cannot be deleted.");
        }
        users.delete(email);
        groups.removeUserFromAll(user);
    }

    /**
     * Changes one field of the user identified by email and password. The new
     * value must satisfy the same rules as user creation.
     */
    public void updateUser(String email, String password, UserField field, String value)
            throws UserOperationException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME:
                user.setFirstName(value);
                break;
            case SURNAME:
                user.setSurnames(value);
                break;
            case EMAIL:
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserOperationException("Invalid email: expected format text@text.xxx.");
                }
                Optional<User> owner = users.findByEmail(value);
                if (owner.isPresent() && owner.get() != user) {
                    throw new UserOperationException("A user with email " + value + " already exists.");
                }
                user.setEmail(value);
                break;
            case PASSWORD:
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserOperationException(
                            "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol.");
                }
                user.setPassword(passwordHasher.hash(value));
                break;
            case PHONE:
                user.setPhone(value);
                break;
            case CARD:
                if (!UserValidator.isValidCard(value)) {
                    throw new UserOperationException("Invalid card number: it must have exactly 16 digits.");
                }
                user.setCard(value);
                break;
            default:
                throw new UserOperationException("Field " + field + " cannot be modified.");
        }
        users.update(email, user);
    }

    private User authenticate(String email, String password) throws UserOperationException {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("No user with email " + email + " exists."));
        if (!passwordHasher.matches(password, user.getPassword())) {
            throw new UserOperationException("Incorrect password.");
        }
        return user;
    }
}
