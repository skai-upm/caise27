package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.UserValidator;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Business rules for registering, modifying and deleting users.
 */
public final class UserController {

    private static UserController instance;

    private UserRepository users;
    private ParkController park;

    private UserController() {
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    /** Injects the dependencies configured at application initialization. */
    public void configure(UserRepository users, ParkController park) {
        this.users = users;
        this.park = park;
    }

    public User create(String firstName, String surnames, String dni, String email, String password, String phone,
            String card) throws ParkException {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException("Invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("Invalid card number");
        }
        if (users.existsDni(dni)) {
            throw new DuplicateException("DNI already registered");
        }
        if (users.existsEmail(email)) {
            throw new DuplicateException("Email already registered");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.add(user);
        return user;
    }

    /** Locates the user by email and checks the password. */
    public User authenticate(String email, String password) throws ParkException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new OperationNotAllowedException("Incorrect password");
        }
        return user;
    }

    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (park.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("User has active tickets");
        }
        if (park.isInLockedGroup(user)) {
            throw new OperationNotAllowedException("User belongs to a group with active group tickets");
        }
        park.removeFromAllGroups(user);
        park.removeFromAllAttractions(user);
        users.remove(user);
    }

    public void update(String email, String password, UserField field, String value) throws ParkException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case PHONE -> user.setPhone(value);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new InvalidDataException("Invalid email");
                }
                User owner = users.findByEmail(value);
                if (owner != null && owner != user) {
                    throw new DuplicateException("Email already registered");
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new InvalidDataException("Invalid password");
                }
                user.setPassword(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new InvalidDataException("Invalid card number");
                }
                user.setCardNumber(value);
            }
        }
        users.update(user);
    }
}
