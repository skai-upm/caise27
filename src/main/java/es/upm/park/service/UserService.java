package es.upm.park.service;

import es.upm.park.model.User;
import es.upm.park.model.UserValidator;
import es.upm.park.repository.UserRepository;

/**
 * Business operations on users: applies validation and uniqueness rules.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
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
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserException("no user with email " + email));
        if (!user.getPassword().equals(password)) {
            throw new UserException("incorrect password");
        }
        repository.remove(user);
        return user;
    }
}
