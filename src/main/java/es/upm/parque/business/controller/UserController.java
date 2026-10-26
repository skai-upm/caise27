package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.UserRepository;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for creating, deleting and updating users. Every successful change is persisted.
 */
public final class UserController {

    private UserRepository repository;
    private List<User> users;

    private UserController() {
    }

    /**
     * Returns the single instance of this controller.
     */
    public static UserController getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Injects the repository and collaborating controllers chosen at startup and loads the stored data.
     */
    public void configure(UserRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.loadAll());
    }

    public List<User> getUsers() {
        requireConfigured();
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        requireConfigured();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        requireConfigured();
        for (User u : users) {
            if (u.getDni().equalsIgnoreCase(dni)) {
                return u;
            }
        }
        return null;
    }

    public void create(String name, String surname, String dni, String email,
                       String password, String phone, String card) {
        requireConfigured();
        requireNotBlank(name, "first name");
        requireNotBlank(surname, "surname");
        requireNotBlank(dni, "DNI");
        requireNotBlank(email, "email");
        requireNotBlank(password, "password");
        requireNotBlank(phone, "telephone");
        requireNotBlank(card, "card number");
        if (!UserValidator.isValidDni(dni)) {
            throw new ParkException("Invalid DNI: " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new ParkException("Invalid email: " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new ParkException("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new ParkException("Invalid card number: it must be exactly 16 digits");
        }
        String normalizedDni = dni.toUpperCase();
        if (findByDni(normalizedDni) != null) {
            throw new ParkException("A user with DNI " + normalizedDni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new ParkException("A user with email " + email + " already exists");
        }
        users.add(new User(name, surname, normalizedDni, email, password, phone, card));
        persist();
    }

    /**
     * Deletes the user after checking the credentials and returns it.
     */
    public User delete(String email, String password) {
        requireConfigured();
        User user = authenticate(email, password);
        users.remove(user);
        persist();
        return user;
    }

    public void update(String email, String password, String fieldName, String newValue) {
        requireConfigured();
        User user = authenticate(email, password);
        if ("DNI".equalsIgnoreCase(fieldName.trim())) {
            throw new ParkException("The DNI cannot be modified");
        }
        UserField field = UserField.parse(fieldName);
        if (field == null) {
            throw new ParkException("Unknown field: " + fieldName);
        }
        requireNotBlank(newValue, "new value");
        switch (field) {
            case NAME -> user.setName(newValue);
            case SURNAME -> user.setSurname(newValue);
            case PHONE -> user.setPhone(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new ParkException("Invalid email: " + newValue);
                }
                User other = findByEmail(newValue);
                if (other != null && other != user) {
                    throw new ParkException("A user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new ParkException("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
                }
                user.setPassword(newValue);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new ParkException("Invalid card number: it must be exactly 16 digits");
                }
                user.setCard(newValue);
            }
        }
        persist();
    }

    /**
     * Returns the user with that email if the password matches; otherwise rejects the operation.
     */
    public User authenticate(String email, String password) {
        requireConfigured();
        User user = findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        if (!user.getPassword().equals(password)) {
            throw new ParkException("Incorrect password");
        }
        return user;
    }

    private void persist() {
        try {
            repository.saveAll(users);
        } catch (UncheckedIOException e) {
            users = new ArrayList<>(repository.loadAll());
            throw new ParkException("Could not save users: " + e.getMessage());
        }
    }

    private static void requireNotBlank(String value, String label) {
        if (!UserValidator.isNotBlank(value)) {
            throw new ParkException("The " + label + " is required");
        }
    }

    private void requireConfigured() {
        if (repository == null) {
            throw new IllegalStateException("UserController is not configured");
        }
    }

    private static final class Holder {
        static final UserController INSTANCE = new UserController();
    }
}
