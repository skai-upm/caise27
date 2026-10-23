package es.upm.park.service;

import es.upm.park.model.User;
import es.upm.park.model.UserField;
import es.upm.park.persistence.UserFileRepository;
import es.upm.park.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * User management rules: creation, removal and modification with
 * validation, uniqueness and credential checks. Every successful change is
 * persisted through the repository.
 */
public class UserService {

    private final UserFileRepository repository;
    private final List<User> users;

    public UserService(UserFileRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.loadAll());
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws OperationException {
        requireNotBlank(firstName, "first name");
        requireNotBlank(surnames, "surnames");
        if (!UserValidator.isValidDni(dni)) {
            throw new OperationException("Invalid DNI: " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "phone");
        requireValidCard(card);
        if (findByDni(dni) != null) {
            throw new OperationException("A user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new OperationException("A user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.add(user);
        persist();
        return user;
    }

    /**
     * Removes an already authenticated user. Callers are responsible for
     * checking that the removal is allowed by the rest of the park state.
     */
    public void remove(User user) {
        users.remove(user);
        persist();
    }

    public void update(String email, String password, String fieldName, String newValue)
            throws OperationException {
        UserField field = UserField.parse(fieldName);
        if (field == null) {
            throw new OperationException("Field " + fieldName + " cannot be modified. Modifiable fields: "
                    + UserField.listNames());
        }
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(newValue, "first name");
                user.setFirstName(newValue);
            }
            case SURNAME -> {
                requireNotBlank(newValue, "surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                requireValidEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new OperationException("A user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValidPassword(newValue);
                user.setPassword(newValue);
            }
            case PHONE -> {
                requireNotBlank(newValue, "phone");
                user.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                user.setCard(newValue);
            }
        }
        persist();
    }

    public User authenticate(String email, String password) throws OperationException {
        User user = findByEmail(email);
        if (user == null) {
            throw new OperationException("No user with email " + email);
        }
        if (!user.hasPassword(password)) {
            throw new OperationException("Incorrect password for " + email);
        }
        return user;
    }

    public void persist() {
        repository.saveAll(users);
    }

    private static void requireNotBlank(String value, String fieldName) throws OperationException {
        if (!UserValidator.isNotBlank(value)) {
            throw new OperationException("The " + fieldName + " must not be empty");
        }
    }

    private static void requireValidEmail(String email) throws OperationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("Invalid email: " + email);
        }
    }

    private static void requireValidPassword(String password) throws OperationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException(
                    "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) throws OperationException {
        if (!UserValidator.isValidCard(card)) {
            throw new OperationException("Invalid card number: it must contain exactly 16 digits");
        }
    }
}
