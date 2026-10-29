package es.upm.amusementpark.business.controller;

import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.AuthenticationException;
import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.UserRepository;

/**
 * Single controller for registered users: creation, authentication, modification and removal.
 * Its repository and password hasher are injected at application initialization.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private UserRepository repository;
    private PasswordHasher hasher;
    private final List<User> users = new ArrayList<>();

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    /** Injects dependencies and loads the stored users. */
    public void configure(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
        users.clear();
        users.addAll(repository.load());
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        requireNonEmpty(firstName, "first name");
        requireNonEmpty(surnames, "surnames");
        requireNonEmpty(phone, "phone");
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException("invalid password");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("invalid card number");
        }
        if (findByDni(dni) != null) {
            throw new DuplicateException("DNI already registered");
        }
        if (findByEmail(email) != null) {
            throw new DuplicateException("email already registered");
        }
        users.add(new User(firstName, surnames, dni, email, hasher.hash(password), phone, card));
        save();
    }

    public void update(String email, String password, String fieldName, String value) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> {
                requireNonEmpty(value, "first name");
                user.setFirstName(value);
            }
            case SURNAME -> {
                requireNonEmpty(value, "surnames");
                user.setSurnames(value);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(value)) {
                    throw new ValidationException("invalid email");
                }
                User other = findByEmail(value);
                if (other != null && other != user) {
                    throw new DuplicateException("email already registered");
                }
                user.setEmail(value);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(value)) {
                    throw new ValidationException("invalid password");
                }
                user.setPasswordHash(hasher.hash(value));
            }
            case PHONE -> {
                requireNonEmpty(value, "phone");
                user.setPhone(value);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(value)) {
                    throw new ValidationException("invalid card number");
                }
                user.setCard(value);
            }
        }
        save();
    }

    /** Returns the user with that email if the password matches its stored hash; otherwise throws. */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("user not found: " + email);
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("incorrect password");
        }
        return user;
    }

    /** Removes an already authenticated user and saves. */
    public void remove(User user) {
        users.remove(user);
        save();
    }

    public void save() {
        repository.save(users);
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    public User requireByEmail(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("user not found: " + email);
        }
        return user;
    }

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    private static void requireNonEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(name + " must not be empty");
        }
    }
}
