package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.AuthenticationException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.UserField;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.AttractionRepository;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * User registration, deletion and modification. Single instance per execution; its
 * repositories and password hasher are injected with {@link #configure}.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();
    private static final String NAME = "UserController";
    private static final String INVALID_PASSWORD =
            "Invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)";

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;
    private SystemState state;
    private PasswordHasher hasher;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets,
                          AttractionRepository attractions, SystemState state, PasswordHasher hasher) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.state = state;
        this.hasher = hasher;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        UserRepository repository = Checks.configured(users, NAME);
        Checks.requireText(firstName, "First name");
        Checks.requireText(surnames, "Surnames");
        validate(UserValidator.isValidDni(dni), "Invalid DNI");
        validate(UserValidator.isValidEmail(email), "Invalid email address");
        validate(UserValidator.isValidPassword(password), INVALID_PASSWORD);
        Checks.requireText(phone, "Phone number");
        validate(UserValidator.isValidCard(card), "Invalid card number (must be 16 digits)");

        String normalizedDni = dni.toUpperCase();
        Checks.require(repository.findByDni(normalizedDni).isEmpty(),
                () -> new DuplicateException("A user with that DNI already exists"));
        Checks.require(repository.findByEmail(email).isEmpty(),
                () -> new DuplicateException("A user with that email already exists"));

        User user = new User(firstName, surnames, normalizedDni, email, hasher.hash(password), phone, card);
        repository.add(user);
        return user;
    }

    /**
     * Deletes a user who has no active tickets and belongs to no locked group. The user is
     * removed from their groups; historical tickets keep referring to them.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        LocalDate today = state.getCurrentDate();
        Checks.require(!tickets.hasActiveTicket(user, today),
                () -> new RuleViolationException("User has active tickets and cannot be deleted"));
        List<Group> memberships = groups.findByMember(user);
        for (Group group : memberships) {
            Checks.require(!tickets.hasActiveGroupTicket(group, today), () -> new RuleViolationException(
                    "User belongs to group " + group.getId() + ", which has active group tickets"));
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        attractions.findContaining(user).ifPresent(attraction -> attraction.exit(user));
        groups.save();
        tickets.save();
        users.remove(user);
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromText(fieldName);
        Checks.require(field != null, () -> new ValidationException("Field cannot be modified: " + fieldName
                + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)"));

        switch (field) {
            case NAME -> {
                Checks.requireText(newValue, "First name");
                user.setFirstName(newValue);
            }
            case SURNAME -> {
                Checks.requireText(newValue, "Surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                validate(UserValidator.isValidEmail(newValue), "Invalid email address");
                users.findByEmail(newValue).ifPresent(other -> Checks.require(other == user,
                        () -> new DuplicateException("A user with that email already exists")));
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                validate(UserValidator.isValidPassword(newValue), INVALID_PASSWORD);
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> {
                Checks.requireText(newValue, "Phone number");
                user.setPhone(newValue);
            }
            case CARD -> {
                validate(UserValidator.isValidCard(newValue), "Invalid card number (must be 16 digits)");
                user.setCard(newValue);
            }
        }
        users.save();
        tickets.save();
    }

    /** The active user with that email, once the password matches the stored hash. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        Checks.require(hasher.matches(password, user.getPasswordHash()),
                () -> new AuthenticationException("Incorrect password"));
        return user;
    }

    public User findByEmail(String email) {
        return Checks.configured(users, NAME).findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private static void validate(boolean condition, String message) {
        Checks.require(condition, () -> new ValidationException(message));
    }
}
