package es.upm.park.business.service;

import java.util.function.Consumer;

import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.business.model.UserValidator;
import es.upm.park.persistence.repository.UserRepository;

/**
 * Business operations on users: applies validation and uniqueness rules.
 */
public class UserService {

    private static final UserService INSTANCE = new UserService();

    private UserRepository repository;

    private UserService() {
    }

    /** The single UserService instance of the execution. */
    public static UserService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies chosen during application initialization.
     *
     * @return this controller
     */
    public UserService configure(UserRepository repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Creates and stores a user after validating its data and checking that
     * neither its DNI nor its email is already in use.
     *
     * @throws UserException when the data is invalid or duplicated
     */
    public User create(String firstName, String surnames, String dni, String email,
            String password, String telephone, String cardNumber) {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("invalid password");
        }
        if (!UserValidator.isValidCardNumber(cardNumber)) {
            throw new UserException("invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new UserException("a user with DNI " + dni + " already exists");
        }
        if (repository.existsByEmail(email)) {
            throw new UserException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, telephone, cardNumber);
        repository.add(user);
        return user;
    }

    /**
     * Deletes the user identified by email after checking its password.
     *
     * @throws UserException when no user has that email or the password is incorrect
     */
    public User delete(String email, String password) {
        User user = authenticate(email, password);
        remove(user);
        return user;
    }

    /**
     * Changes one modifiable field of the user identified by email and
     * password. New values must satisfy the creation rules.
     *
     * @return the updated user
     * @throws UserException when authentication fails, the field is not
     *         modifiable or the new value is invalid or duplicated
     */
    public User update(String email, String password, String fieldName, String value) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new UserException("field " + fieldName + " cannot be modified");
        }
        repository.update(user, change(user, field, value));
        return user;
    }

    private Consumer<User> change(User user, UserField field, String value) {
        return switch (field) {
            case NAME -> u -> u.setFirstName(value);
            case SURNAME -> u -> u.setSurnames(value);
            case PHONE -> u -> u.setTelephone(value);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserException("invalid email");
                }
                repository.findByEmail(value).filter(other -> other != user).ifPresent(other -> {
                    throw new UserException("a user with email " + value + " already exists");
                });
                yield u -> u.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserException("invalid password");
                }
                yield u -> u.setPassword(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCardNumber(value)) {
                    throw new UserException("invalid card number");
                }
                yield u -> u.setCardNumber(value);
            }
        };
    }

    /**
     * Returns the user with that email if the password matches.
     *
     * @throws UserException when no user has that email or the password is incorrect
     */
    public User authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserException("no user with email " + email));
        if (!user.getPassword().equals(password)) {
            throw new UserException("incorrect password");
        }
        return user;
    }

    /** Removes an already authorized user from the stored users. */
    public void remove(User user) {
        repository.remove(user);
    }
}
