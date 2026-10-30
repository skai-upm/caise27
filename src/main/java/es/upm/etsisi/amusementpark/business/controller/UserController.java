package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.business.validation.UserValidator;
import es.upm.etsisi.amusementpark.persistence.UserRepository;

import java.util.List;

/**
 * User management (Singleton). Passwords are validated, then stored only through the injected hasher.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private ParkController park;
    private PasswordHasher hasher;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park, PasswordHasher hasher) {
        this.park = park;
        this.hasher = hasher;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String cardNumber) {
        UserRepository repository = park.users();
        check(UserValidator.isValidDni(dni), "Invalid DNI");
        check(UserValidator.isValidEmail(email), "Invalid email");
        check(UserValidator.isValidPassword(password), "Invalid password");
        check(UserValidator.isValidCardNumber(cardNumber), "Invalid card number");
        if (repository.findByDni(dni).isPresent()) {
            throw new DuplicateException("DNI already registered");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new DuplicateException("Email already registered");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, cardNumber);
        repository.add(user);
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        if (park.tickets().hasActiveTicketFor(user, park.today())) {
            throw new OperationNotAllowedException("User has active tickets");
        }
        List<Group> groups = park.groups().findByMember(user);
        for (Group group : groups) {
            if (park.tickets().hasActiveGroupTicket(group, park.today())) {
                throw new OperationNotAllowedException("User belongs to locked group " + group.getId());
            }
        }
        groups.forEach(g -> g.remove(user));
        park.users().remove(user);
        park.groups().save();
        park.tickets().save();
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("Field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                check(UserValidator.isValidEmail(newValue), "Invalid email");
                park.users().findByEmail(newValue)
                        .filter(other -> other != user)
                        .ifPresent(other -> {
                            throw new DuplicateException("Email already registered");
                        });
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                check(UserValidator.isValidPassword(newValue), "Invalid password");
                user.setPasswordHash(hasher.hash(newValue));
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                check(UserValidator.isValidCardNumber(newValue), "Invalid card number");
                user.setCardNumber(newValue);
            }
        }
        park.users().update(user);
        park.tickets().save();
    }

    public User findActive(String email) {
        return park.users().findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private User authenticate(String email, String password) {
        User user = park.users().findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password");
        }
        return user;
    }

    private static void check(boolean valid, String message) {
        if (!valid) {
            throw new ValidationException(message);
        }
    }
}
