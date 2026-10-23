package es.upm.etsisi.park.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.persistence.UserRepository;

/**
 * User management operations. Every successful change is persisted through
 * the repository; if saving fails the in-memory change is undone.
 */
public class UserService {

    private final UserRepository repository;
    private final List<User> users;

    public UserService(UserRepository repository) throws IOException {
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

    public User create(String firstName, String surnames, String dni, String email, String password,
                       String phone, String card) throws UserException {
        requireNonEmpty(firstName, "First name");
        requireNonEmpty(surnames, "Surnames");
        if (!UserValidator.isValidDni(dni)) {
            throw new UserException("Invalid DNI '" + dni + "': expected 8 digits and the matching control letter");
        }
        validate(UserField.EMAIL, email);
        validate(UserField.PASSWORD, password);
        validate(UserField.PHONE, phone);
        validate(UserField.CARD, card);
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            throw new UserException("A user with DNI " + normalizedDni + " is already registered");
        }
        if (findByEmail(email) != null) {
            throw new UserException("A user with email " + email + " is already registered");
        }
        User user = new User(firstName, surnames, normalizedDni, email, password, phone, card);
        users.add(user);
        try {
            persist();
        } catch (UserException e) {
            users.remove(user);
            throw e;
        }
        return user;
    }

    public void delete(String email, String password) throws UserException {
        User user = authenticate(email, password);
        int index = users.indexOf(user);
        users.remove(index);
        try {
            persist();
        } catch (UserException e) {
            users.add(index, user);
            throw e;
        }
    }

    public void update(String email, String password, UserField field, String newValue) throws UserException {
        User user = authenticate(email, password);
        validate(field, newValue);
        if (field == UserField.EMAIL) {
            User other = findByEmail(newValue);
            if (other != null && other != user) {
                throw new UserException("A user with email " + newValue + " is already registered");
            }
        }
        String oldValue = user.get(field);
        user.set(field, newValue);
        try {
            persist();
        } catch (UserException e) {
            user.set(field, oldValue);
            throw e;
        }
    }

    private User authenticate(String email, String password) throws UserException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserException("User with email " + email + " not found");
        }
        if (!user.checkPassword(password)) {
            throw new UserException("Incorrect password for " + email);
        }
        return user;
    }

    private void validate(UserField field, String value) throws UserException {
        switch (field) {
            case NAME -> requireNonEmpty(value, "First name");
            case SURNAME -> requireNonEmpty(value, "Surnames");
            case PHONE -> requireNonEmpty(value, "Phone");
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new UserException("Invalid email '" + value + "': expected text@text.xxx");
                }
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new UserException(
                            "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new UserException("Invalid card number '" + value + "': expected exactly 16 digits");
                }
            }
        }
    }

    private static void requireNonEmpty(String value, String label) throws UserException {
        if (!UserValidator.isNonEmpty(value)) {
            throw new UserException(label + " must not be empty");
        }
    }

    private void persist() throws UserException {
        try {
            repository.save(users);
        } catch (IOException e) {
            throw new UserException("Could not save users to " + repository.getFile() + ": " + e.getMessage());
        }
    }
}
