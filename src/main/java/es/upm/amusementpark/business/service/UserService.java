package es.upm.amusementpark.business.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.DataAccessException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.UserValidator;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.file.UserStorage;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * User management operations: applies validation and uniqueness rules before touching storage.
 */
public class UserService {

    private UserRepository repository;
    private UserStorage storage;

    private static final UserService INSTANCE = new UserService();

    private UserService() {
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies of this controller; called during application initialization.
     */
    public UserService configure(UserRepository repository, UserStorage storage) {
        this.repository = repository;
        this.storage = storage;
        return this;
    }

    public Optional<User> findByDni(String dni) {
        return repository.findByDni(dni);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User createUser(String firstName, String surnames, String dni, String email, String password,
            String phone, String cardNumber) throws ParkException {
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException("Invalid password");
        }
        if (!UserValidator.isValidCardNumber(cardNumber)) {
            throw new ValidationException("Invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new ConflictException("A user with this DNI already exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new ConflictException("A user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, PasswordHasher.hash(password), phone, cardNumber);
        List<User> users = repository.findAll();
        users.add(user);
        persist(users);
        repository.save(user);
        return user;
    }

    /**
     * Deletes the user identified by email and password.
     */
    public void deleteUser(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        List<User> users = repository.findAll();
        users.remove(user);
        persist(users);
        repository.delete(user.getDni());
    }

    /**
     * Changes one modifiable field of the user identified by email and password. The new value must
     * satisfy the same rules as on creation.
     */
    public void updateUser(String email, String password, UserField field, String value)
            throws ParkException {
        User user = authenticate(email, password);
        User updated = copy(user);
        switch (field) {
            case NAME -> updated.setFirstName(value);
            case SURNAME -> updated.setSurnames(value);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new ValidationException("Invalid email");
                }
                if (repository.findByEmail(value).filter(other -> other != user).isPresent()) {
                    throw new ConflictException("A user with this email already exists");
                }
                updated.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new ValidationException("Invalid password");
                }
                updated.setPassword(PasswordHasher.hash(value));
            }
            case PHONE -> updated.setPhone(value);
            case CARD -> {
                if (!UserValidator.isValidCardNumber(value)) {
                    throw new ValidationException("Invalid card number");
                }
                updated.setCardNumber(value);
            }
        }
        List<User> users = repository.findAll();
        users.set(users.indexOf(user), updated);
        persist(users);
        copyInto(updated, user);
    }

    private static void copyInto(User from, User to) {
        to.setFirstName(from.getFirstName());
        to.setSurnames(from.getSurnames());
        to.setEmail(from.getEmail());
        to.setPassword(from.getPassword());
        to.setPhone(from.getPhone());
        to.setCardNumber(from.getCardNumber());
    }

    private static User copy(User u) {
        return new User(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(), u.getPassword(), u.getPhone(),
                u.getCardNumber());
    }

    /**
     * Returns the user identified by email and password.
     */
    public User authenticate(String email, String password) throws ParkException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!PasswordHasher.matches(password, user.getPassword())) {
            throw new OperationNotAllowedException("Incorrect password");
        }
        return user;
    }

    private void persist(List<User> users) throws ParkException {
        if (storage == null) {
            return;
        }
        try {
            storage.save(users);
        } catch (IOException e) {
            throw new DataAccessException("Could not save users: " + e.getMessage(), e);
        }
    }
}
