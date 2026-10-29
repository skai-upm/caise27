package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.validation.UserValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory registry of active users: creation and modification with validation and uniqueness.
 * Persistence and cross-entity rules (tickets, groups) are coordinated by {@link Park}.
 */
public class UserService {

    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    /**
     * Replaces the registered users with the given ones.
     */
    public void load(List<User> users) {
        usersByEmail.clear();
        for (User user : users) {
            usersByEmail.put(user.getEmail(), user);
        }
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws OperationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new OperationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new OperationException("a user with this DNI already exists");
        }
        if (usersByEmail.containsKey(email)) {
            throw new OperationException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        usersByEmail.put(email, user);
        return user;
    }

    public void update(String email, String password, UserField field, String value)
            throws OperationException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case PHONE -> user.setPhone(value);
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new OperationException("invalid password");
                }
                user.setPassword(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new OperationException("invalid card number");
                }
                user.setCard(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new OperationException("invalid email");
                }
                User owner = usersByEmail.get(value);
                if (owner != null && owner != user) {
                    throw new OperationException("a user with this email already exists");
                }
                usersByEmail.remove(user.getEmail());
                user.setEmail(value);
                usersByEmail.put(value, user);
            }
        }
    }

    /**
     * Returns the user with this email if the password matches.
     */
    public User authenticate(String email, String password) throws OperationException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw new OperationException("user not found");
        }
        if (!user.checkPassword(password)) {
            throw new OperationException("incorrect password");
        }
        return user;
    }

    public void remove(User user) {
        usersByEmail.remove(user.getEmail(), user);
    }

    public boolean isActive(User user) {
        return usersByEmail.get(user.getEmail()) == user;
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    public User findByDni(String dni) {
        for (User user : usersByEmail.values()) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return new ArrayList<>(usersByEmail.values());
    }
}
