package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.ConflictException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.business.model.UserValidator;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import java.util.ArrayList;
import java.util.List;

/**
 * Single manager of the active users: creation, modification, authentication and removal.
 * Its repository and password hasher are injected with {@link #configure}.
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

    /**
     * Injects the dependencies and loads the stored users.
     */
    public void configure(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
        users.clear();
        users.addAll(repository.load());
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

    public User findByDni(String dni) {
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    public User requireByEmail(String email) throws NotFoundException {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("user not found: " + email);
        }
        return user;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ParkException {
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
            throw new ConflictException("a user with this DNI already exists");
        }
        if (findByEmail(email) != null) {
            throw new ConflictException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        users.add(user);
        repository.save(users);
        return user;
    }

    /**
     * Removes an active user. Callers are responsible for checking that removal is allowed.
     */
    public void remove(User user) {
        users.remove(user);
        repository.save(users);
    }

    public void save() {
        repository.save(users);
    }

    public void update(String email, String password, String fieldName, String newValue) throws ParkException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("field cannot be modified: " + fieldName);
        }
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new ValidationException("invalid email");
                }
                User other = findByEmail(newValue);
                if (other != null && other != user) {
                    throw new ConflictException("a user with this email already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new ValidationException("invalid password");
                }
                user.setPassword(hasher.hash(newValue));
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new ValidationException("invalid card number");
                }
                user.setCard(newValue);
            }
        }
        repository.save(users);
    }

    public User authenticate(String email, String password) throws ParkException {
        User user = requireByEmail(email);
        if (!hasher.matches(password, user.getPassword())) {
            throw new AuthenticationException("incorrect password");
        }
        return user;
    }
}
