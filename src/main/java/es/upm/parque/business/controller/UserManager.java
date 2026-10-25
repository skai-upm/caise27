package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.UserRepository;

/**
 * Business rules for creating, deleting and updating users. Every successful change is persisted.
 */
public class UserManager {

    public static final String USER_CREATED = "OK USER CREATED";
    public static final String USER_DELETED = "OK USER DELETED";
    public static final String USER_UPDATED = "OK USER UPDATED";
    public static final String EMPTY_FIELD = "ERROR EMPTY FIELD";
    public static final String INVALID_DNI = "ERROR INVALID DNI";
    public static final String INVALID_EMAIL = "ERROR INVALID EMAIL";
    public static final String INVALID_PASSWORD = "ERROR INVALID PASSWORD";
    public static final String INVALID_CARD = "ERROR INVALID CARD";
    public static final String INVALID_FIELD = "ERROR INVALID FIELD";
    public static final String DUPLICATE_DNI = "ERROR DUPLICATE DNI";
    public static final String DUPLICATE_EMAIL = "ERROR DUPLICATE EMAIL";
    public static final String INVALID_CREDENTIALS = "ERROR INVALID CREDENTIALS";
    public static final String STORAGE_FAILURE = "ERROR STORAGE FAILURE";

    private static UserManager instance;

    private final UserRepository repository;
    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    /**
     * Creates the unique UserManager with its injected dependencies, replacing any previous instance.
     * Called once at application startup.
     */
    public static UserManager initialize(UserRepository repository) throws IOException {
        instance = new UserManager(repository);
        return instance;
    }

    /**
     * Returns the unique UserManager; UserManager.initialize must have been called first.
     */
    public static UserManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserManager not initialized");
        }
        return instance;
    }

    private UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        for (User user : repository.load()) {
            usersByDni.put(user.getDni(), user);
        }
    }

    public String create(User user) {
        if (!UserValidator.isNotBlank(user.getName()) || !UserValidator.isNotBlank(user.getSurname())
                || !UserValidator.isNotBlank(user.getPhone())) {
            return EMPTY_FIELD;
        }
        if (!UserValidator.isValidDni(user.getDni())) {
            return INVALID_DNI;
        }
        if (!UserValidator.isValidEmail(user.getEmail())) {
            return INVALID_EMAIL;
        }
        if (!UserValidator.isValidPassword(user.getPassword())) {
            return INVALID_PASSWORD;
        }
        if (!UserValidator.isValidCard(user.getCard())) {
            return INVALID_CARD;
        }
        if (usersByDni.containsKey(user.getDni())) {
            return DUPLICATE_DNI;
        }
        if (findByEmail(user.getEmail()) != null) {
            return DUPLICATE_EMAIL;
        }
        usersByDni.put(user.getDni(), user);
        return persist(USER_CREATED);
    }

    public String delete(String email, String password) {
        User user = authenticate(email, password);
        if (user == null) {
            return INVALID_CREDENTIALS;
        }
        usersByDni.remove(user.getDni());
        return persist(USER_DELETED);
    }

    public String update(String email, String password, String fieldName, String value) {
        User user = authenticate(email, password);
        if (user == null) {
            return INVALID_CREDENTIALS;
        }
        UserField field = UserField.parse(fieldName);
        if (field == null) {
            return INVALID_FIELD;
        }
        String error = validateNewValue(user, field, value);
        if (error != null) {
            return error;
        }
        switch (field) {
            case NAME -> user.setName(value);
            case SURNAME -> user.setSurname(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
        return persist(USER_UPDATED);
    }

    public User findByEmail(String email) {
        for (User user : usersByDni.values()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public User findByDni(String dni) {
        return usersByDni.get(dni);
    }

    public int count() {
        return usersByDni.size();
    }

    private String validateNewValue(User user, UserField field, String value) {
        return switch (field) {
            case NAME, SURNAME, PHONE -> UserValidator.isNotBlank(value) ? null : EMPTY_FIELD;
            case EMAIL -> validateNewEmail(user, value);
            case PASSWORD -> UserValidator.isValidPassword(value) ? null : INVALID_PASSWORD;
            case CARD -> UserValidator.isValidCard(value) ? null : INVALID_CARD;
        };
    }

    private String validateNewEmail(User user, String email) {
        if (!UserValidator.isValidEmail(email)) {
            return INVALID_EMAIL;
        }
        User owner = findByEmail(email);
        return owner == null || owner == user ? null : DUPLICATE_EMAIL;
    }

    /**
     * Returns the user with this email when the password matches, or null otherwise.
     */
    public User checkCredentials(String email, String password) {
        return authenticate(email, password);
    }

    private User authenticate(String email, String password) {
        User user = findByEmail(email);
        return user != null && user.getPassword().equals(password) ? user : null;
    }

    private String persist(String success) {
        try {
            repository.save(usersByDni.values());
            return success;
        } catch (IOException e) {
            return STORAGE_FAILURE;
        }
    }
}
