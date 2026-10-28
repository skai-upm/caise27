package es.upm.amusementpark.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.UserValidator;
import es.upm.amusementpark.persistence.UserStorage;
import es.upm.amusementpark.repository.UserRepository;

/**
 * User management operations: applies validation and uniqueness rules before touching storage.
 */
public class UserService {

    private final UserRepository repository;
    private final UserStorage storage;

    public UserService(UserRepository repository, UserStorage storage) {
        this.repository = repository;
        this.storage = storage;
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
            String phone, String cardNumber) throws OperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException("Invalid password");
        }
        if (!UserValidator.isValidCardNumber(cardNumber)) {
            throw new OperationException("Invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new OperationException("A user with this DNI already exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new OperationException("A user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, cardNumber);
        List<User> users = repository.findAll();
        users.add(user);
        persist(users);
        repository.save(user);
        return user;
    }

    /**
     * Deletes the user identified by email and password.
     */
    public void deleteUser(String email, String password) throws OperationException {
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
            throws OperationException {
        User user = authenticate(email, password);
        User updated = copy(user);
        switch (field) {
            case NAME -> updated.setFirstName(value);
            case SURNAME -> updated.setSurnames(value);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new OperationException("Invalid email");
                }
                if (repository.findByEmail(value).filter(other -> other != user).isPresent()) {
                    throw new OperationException("A user with this email already exists");
                }
                updated.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new OperationException("Invalid password");
                }
                updated.setPassword(value);
            }
            case PHONE -> updated.setPhone(value);
            case CARD -> {
                if (!UserValidator.isValidCardNumber(value)) {
                    throw new OperationException("Invalid card number");
                }
                updated.setCardNumber(value);
            }
        }
        List<User> users = repository.findAll();
        users.set(users.indexOf(user), updated);
        persist(users);
        repository.save(updated);
    }

    private static User copy(User u) {
        return new User(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(), u.getPassword(), u.getPhone(),
                u.getCardNumber());
    }

    private User authenticate(String email, String password) throws OperationException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new OperationException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw new OperationException("Incorrect password");
        }
        return user;
    }

    private void persist(List<User> users) throws OperationException {
        try {
            storage.save(users);
        } catch (IOException e) {
            throw new OperationException("Could not save users: " + e.getMessage());
        }
    }
}
