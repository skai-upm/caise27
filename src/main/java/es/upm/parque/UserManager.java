package es.upm.parque;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business rules for creating, deleting and updating users. Every successful change is persisted.
 */
public class UserManager {

    static final String USER_CREATED = "OK USER CREATED";
    static final String USER_DELETED = "OK USER DELETED";
    static final String USER_UPDATED = "OK USER UPDATED";
    static final String EMPTY_FIELD = "ERROR EMPTY FIELD";
    static final String INVALID_DNI = "ERROR INVALID DNI";
    static final String INVALID_EMAIL = "ERROR INVALID EMAIL";
    static final String INVALID_PASSWORD = "ERROR INVALID PASSWORD";
    static final String INVALID_CARD = "ERROR INVALID CARD";
    static final String INVALID_FIELD = "ERROR INVALID FIELD";
    static final String DUPLICATE_DNI = "ERROR DUPLICATE DNI";
    static final String DUPLICATE_EMAIL = "ERROR DUPLICATE EMAIL";
    static final String INVALID_CREDENTIALS = "ERROR INVALID CREDENTIALS";
    static final String STORAGE_FAILURE = "ERROR STORAGE FAILURE";

    private final UserRepository repository;
    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    public UserManager(UserRepository repository) throws IOException {
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
