package es.upm.amusementpark.service;

import java.io.IOException;
import java.util.List;

import es.upm.amusementpark.model.User;
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

    public User createUser(String firstName, String surnames, String dni, String email, String password,
            String phone, String cardNumber) throws UserOperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserOperationException("Invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("Invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException("Invalid password");
        }
        if (!UserValidator.isValidCardNumber(cardNumber)) {
            throw new UserOperationException("Invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new UserOperationException("A user with this DNI already exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new UserOperationException("A user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, cardNumber);
        List<User> users = repository.findAll();
        users.add(user);
        persist(users);
        repository.save(user);
        return user;
    }

    private void persist(List<User> users) throws UserOperationException {
        try {
            storage.save(users);
        } catch (IOException e) {
            throw new UserOperationException("Could not save users: " + e.getMessage());
        }
    }
}
