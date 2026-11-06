package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.model.User;
import es.upm.park.business.security.PasswordService;
import es.upm.park.business.validation.UserValidator;

/**
 * User management controller (single instance per execution). It coordinates
 * validation, uniqueness, password protection and storage of users, using the
 * user store and password protection injected at application initialization.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private UserRegistry registry;
    private PasswordService passwords;
    private UserStorage storage = UserStorage.NONE;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    /** Injects the user store and the password protection, without persistence. */
    public void configure(UserRegistry registry, PasswordService passwords) {
        configure(registry, passwords, UserStorage.NONE);
    }

    /** Injects the user store, the password protection and where users are persisted. */
    public void configure(UserRegistry registry, PasswordService passwords, UserStorage storage) {
        this.registry = registry;
        this.passwords = passwords;
        this.storage = storage;
    }

    /** Registers a new user after validating its data and the uniqueness of DNI and email. */
    public User createUser(String firstName, String surnames, String dni, String email, String rawPassword,
                           String phone, String card) {
        requireConfigured();
        UserValidator.validateUser(firstName, surnames, dni, email, rawPassword, phone, card);
        if (registry.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("A user with email " + email + " already exists");
        }
        if (registry.findByDni(dni).isPresent()) {
            throw new BusinessRuleException("A user with DNI " + dni + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, passwords.protect(rawPassword), phone, card);
        registry.save(user);
        save();
        return user;
    }

    private void save() {
        storage.save(registry.all());
    }

    private void requireConfigured() {
        if (registry == null || passwords == null) {
            throw new IllegalStateException("UserController is not configured with a user store and password service");
        }
    }
}
