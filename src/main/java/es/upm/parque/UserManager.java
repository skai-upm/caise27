package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for users: validation, uniqueness and credential checks.
 * Every successful change is saved through the repository.
 */
public class UserManager {
    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
    }

    public User create(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        requireNotBlank(name, "first name");
        requireNotBlank(surname, "surname");
        requireNotBlank(phone, "telephone number");
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("invalid DNI '" + dni + "'");
        }
        requireValidEmail(email);
        requireValidPassword(password);
        requireValidCard(card);
        if (findByDni(dni) != null) {
            throw new IllegalArgumentException("a user with DNI " + dni + " already exists");
        }
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("a user with email " + email + " already exists");
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        users.remove(user);
        repository.save(users);
    }

    public User update(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> {
                requireNotBlank(value, "first name");
                user.setName(value);
            }
            case SURNAME -> {
                requireNotBlank(value, "surname");
                user.setSurname(value);
            }
            case EMAIL -> {
                requireValidEmail(value);
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new IllegalArgumentException("email " + value + " belongs to another user");
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                requireValidPassword(value);
                user.setPassword(value);
            }
            case PHONE -> {
                requireNotBlank(value, "telephone number");
                user.setPhone(value);
            }
            case CARD -> {
                requireValidCard(value);
                user.setCard(value);
            }
        }
        repository.save(users);
        return user;
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("incorrect password for " + email);
        }
        return user;
    }

    private static void requireNotBlank(String value, String what) {
        if (!UserValidator.isNotBlank(value)) {
            throw new IllegalArgumentException("the " + what + " cannot be empty");
        }
    }

    private static void requireValidEmail(String email) {
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("invalid email '" + email + "'");
        }
    }

    private static void requireValidPassword(String password) {
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void requireValidCard(String card) {
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("invalid card number '" + card + "': it must have exactly 16 digits");
        }
    }
}
