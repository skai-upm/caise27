package es.upm.parque.business;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.UserRepository;

/**
 * Business operations on users: registration, deletion and modification.
 */
public class UserManager {

    private static final String INVALID_PASSWORD = "invalid password (requires uppercase, lowercase, digit and symbol)";
    private static final String INVALID_CARD = "invalid card number (requires exactly 16 digits)";

    private ParkController park;
    private PasswordProtector passwords;

    private UserManager() {
    }

    private static final class Holder {
        private static final UserManager INSTANCE = new UserManager();
    }

    /** Returns the only instance of this controller. */
    public static UserManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management and the password protection configured during initialization. */
    public void inject(ParkController park, PasswordProtector passwords) {
        this.park = park;
        this.passwords = passwords;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    private PasswordProtector passwords() {
        return ParkConfiguration.requireInjected(passwords);
    }

    private UserRepository users() {
        return park().getUsers();
    }

    /** Registers a new user after validating its data and the uniqueness of DNI and email. */
    public User create(String firstName, String surnames, String dni, String email, String password, String phone,
                       String card) throws ParkException {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("invalid DNI '" + dni + "'");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email '" + email + "'");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException(INVALID_PASSWORD);
        }
        if (!UserValidator.isValidCardNumber(card)) {
            throw new InvalidDataException(INVALID_CARD);
        }
        if (users().existsByDni(dni)) {
            throw duplicate("DNI", dni);
        }
        if (users().existsByEmail(email)) {
            throw duplicate("email", email);
        }
        User user = new User(firstName, surnames, dni, email, passwords().protect(password), phone, card);
        users().add(user);
        park().persist();
        return user;
    }

    /** Returns the user identified by the email whose password matches. */
    public User authenticate(String email, String password) throws ParkException {
        User user = users().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        if (!passwords().verify(password, user.getPassword())) {
            throw new InvalidDataException("incorrect password");
        }
        return user;
    }

    /** Deletes the authenticated user unless active tickets or a locked group prevent it. */
    public User delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (park().hasActiveTicket(user)) {
            throw new RuleViolationException("user " + user.getEmail()
                    + " has a ticket for today or a later date and cannot be deleted");
        }
        if (park().belongsToLockedGroup(user)) {
            throw new RuleViolationException("user " + user.getEmail() + " belongs to a group with an active group ticket");
        }
        park().getGroups().removeUserEverywhere(user);
        users().remove(user.getDni());
        park().persist();
        return user;
    }

    /** Changes one modifiable field of the authenticated user, applying the creation rules to the new value. */
    public User update(String email, String password, String fieldName, String value) throws ParkException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new InvalidDataException("field '" + fieldName
                    + "' cannot be modified (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        checkNewValue(user, field, value);
        applyField(user, field, value);
        users().update(user);
        park().persist();
        return user;
    }

    private void checkNewValue(User user, UserField field, String value) throws ParkException {
        switch (field) {
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new InvalidDataException("invalid email '" + value + "'");
                }
                User owner = users().findByEmail(value);
                if (owner != null && owner != user) {
                    throw duplicate("email", value);
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new InvalidDataException(INVALID_PASSWORD);
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCardNumber(value)) {
                    throw new InvalidDataException(INVALID_CARD);
                }
            }
            default -> {
                // NAME, SURNAME and PHONE have no format rules
            }
        }
    }

    private void applyField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(passwords().protect(value));
            case PHONE -> user.setTelephone(value);
            case CARD -> user.setCardNumber(value);
        }
    }

    private static RuleViolationException duplicate(String attribute, String value) {
        return new RuleViolationException("a user with " + attribute + " " + value + " already exists");
    }
}
