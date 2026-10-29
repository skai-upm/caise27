package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.validation.UserValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User operations: creation, deletion and modification, with validation, uniqueness and persistence.
 */
public class UserService {

    private final UserRepository repository;
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        for (User user : repository.load()) {
            usersByEmail.put(user.getEmail(), user);
        }
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws UserOperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserOperationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new UserOperationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new UserOperationException("a user with this DNI already exists");
        }
        if (usersByEmail.containsKey(email)) {
            throw new UserOperationException("a user with this email already exists");
        }
        usersByEmail.put(email, new User(firstName, surnames, dni, email, password, phone, card));
        persist();
    }

    public void delete(String email, String password) throws UserOperationException {
        User user = authenticate(email, password);
        usersByEmail.remove(user.getEmail());
        persist();
    }

    public void update(String email, String password, UserField field, String value)
            throws UserOperationException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case PHONE -> user.setPhone(value);
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserOperationException("invalid password");
                }
                user.setPassword(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserOperationException("invalid card number");
                }
                user.setCard(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserOperationException("invalid email");
                }
                User owner = usersByEmail.get(value);
                if (owner != null && owner != user) {
                    throw new UserOperationException("a user with this email already exists");
                }
                usersByEmail.remove(user.getEmail());
                user.setEmail(value);
                usersByEmail.put(value, user);
            }
        }
        persist();
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }

    private User findByDni(String dni) {
        for (User user : usersByEmail.values()) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    private User authenticate(String email, String password) throws UserOperationException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new UserOperationException("user not found");
        }
        if (!user.checkPassword(password)) {
            throw new UserOperationException("incorrect password");
        }
        return user;
    }

    private void persist() throws UserOperationException {
        try {
            repository.saveAll(usersByEmail.values());
        } catch (IOException e) {
            throw new UserOperationException("could not save users: " + e.getMessage());
        }
    }
}
