package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the user-management rules (validation, uniqueness, credentials) and keeps the repository up to date.
 * Every operation returns a message starting with "OK" or "ERROR".
 */
public class UserManager {

    private final UserRepository repository;
    private final List<User> users;

    public UserManager(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.load());
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

    public String createUser(String name, String surname, String dni, String email, String password, String phone,
            String card) {
        if (!UserValidator.isNotBlank(name) || !UserValidator.isNotBlank(surname)
                || !UserValidator.isNotBlank(phone)) {
            return "ERROR Name, surname and telephone must not be empty";
        }
        if (!UserValidator.isValidDni(dni)) {
            return "ERROR Invalid DNI: " + dni;
        }
        if (!UserValidator.isValidEmail(email)) {
            return "ERROR Invalid email: " + email;
        }
        if (!UserValidator.isValidPassword(password)) {
            return "ERROR Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol";
        }
        if (!UserValidator.isValidCard(card)) {
            return "ERROR Invalid card number: it must have exactly 16 digits";
        }
        if (findByDni(dni) != null) {
            return "ERROR A user with DNI " + dni + " already exists";
        }
        if (findByEmail(email) != null) {
            return "ERROR A user with email " + email + " already exists";
        }
        User user = new User(name, surname, dni, email, password, phone, card);
        users.add(user);
        if (!persist()) {
            users.remove(user);
            return "ERROR Could not save users";
        }
        return "OK USER CREATED " + email;
    }

    /**
     * Removes the user and updates the storage. Returns false (leaving the user in place) if saving fails.
     */
    public boolean remove(User user) {
        int index = users.indexOf(user);
        if (index < 0) {
            return false;
        }
        users.remove(index);
        if (!persist()) {
            users.add(index, user);
            return false;
        }
        return true;
    }

    public String updateUser(String email, String password, UserField field, String value) {
        User user = authenticate(email, password);
        if (user == null) {
            return "ERROR Unknown email or wrong password";
        }
        String error = validateNewValue(user, field, value);
        if (error != null) {
            return error;
        }
        String previous = get(user, field);
        set(user, field, value);
        if (!persist()) {
            set(user, field, previous);
            return "ERROR Could not save users";
        }
        return "OK USER UPDATED " + field;
    }

    /**
     * Returns the user with that email if the password matches, or {@code null} otherwise.
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null || !user.passwordMatches(password)) {
            return null;
        }
        return user;
    }

    public User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    private String validateNewValue(User user, UserField field, String value) {
        if (!UserValidator.isNotBlank(value)) {
            return "ERROR The new value must not be empty";
        }
        switch (field) {
            case EMAIL:
                if (!UserValidator.isValidEmail(value)) {
                    return "ERROR Invalid email: " + value;
                }
                User owner = findByEmail(value);
                if (owner != null && owner != user) {
                    return "ERROR A user with email " + value + " already exists";
                }
                return null;
            case PASSWORD:
                return UserValidator.isValidPassword(value) ? null
                        : "ERROR Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol";
            case CARD:
                return UserValidator.isValidCard(value) ? null
                        : "ERROR Invalid card number: it must have exactly 16 digits";
            default:
                return null;
        }
    }

    private static String get(User user, UserField field) {
        switch (field) {
            case NAME:
                return user.getName();
            case SURNAME:
                return user.getSurname();
            case EMAIL:
                return user.getEmail();
            case PASSWORD:
                return user.getPassword();
            case PHONE:
                return user.getPhone();
            default:
                return user.getCard();
        }
    }

    private static void set(User user, UserField field, String value) {
        switch (field) {
            case NAME:
                user.setName(value);
                break;
            case SURNAME:
                user.setSurname(value);
                break;
            case EMAIL:
                user.setEmail(value);
                break;
            case PASSWORD:
                user.setPassword(value);
                break;
            case PHONE:
                user.setPhone(value);
                break;
            default:
                user.setCard(value);
                break;
        }
    }

    private boolean persist() {
        try {
            repository.saveAll(users);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
