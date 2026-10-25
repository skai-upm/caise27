package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates, deletes and updates users, enforcing validation and uniqueness, and keeps
 * the repository in sync after every successful change.
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
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
            if (user.getDni().equalsIgnoreCase(dni)) {
                return user;
            }
        }
        return null;
    }

    public User create(String name, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNotBlank(name, "first name");
        requireNotBlank(surnames, "surnames");
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("invalid DNI " + dni);
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireNotBlank(phone, "telephone");
        requireValidCard(card);
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            throw new UserException("a user with DNI " + normalizedDni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new UserException("a user with email " + email + " already exists");
        }
        User user = new User(name, surnames, normalizedDni, email, password, phone, card);
        users.add(user);
        repository.saveAll(users);
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        users.remove(user);
        repository.saveAll(users);
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(newValue, "first name");
                user.setName(newValue);
            }
            case SURNAME -> {
                requireNotBlank(newValue, "surnames");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                requireValidEmail(newValue);
                User owner = findByEmail(newValue);
                if (owner != null && owner != user) {
                    throw new UserException("a user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValidPassword(newValue);
                user.setPassword(newValue);
            }
            case PHONE -> {
                requireNotBlank(newValue, "telephone");
                user.setPhone(newValue);
            }
            case CARD -> {
                requireValidCard(newValue);
                user.setCard(newValue);
            }
        }
        repository.saveAll(users);
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("no user with email " + email);
        }
        if (!user.passwordMatches(password)) {
            throw new UserException("incorrect password");
        }
        return user;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (!UserValidator.isNotBlank(value)) {
            throw new UserException(fieldName + " must not be empty");
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("invalid email " + email);
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("invalid card number: it must have exactly 16 digits");
        }
    }
}
