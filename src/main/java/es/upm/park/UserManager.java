package es.upm.park;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Creates, deletes and updates users. Every successful change is persisted before it
 * becomes visible in memory, so a rejected or failed operation never changes state.
 */
public class UserManager {

    private final UserFileStore store;
    private List<User> users;

    public UserManager(UserFileStore store) throws IOException {
        this.store = store;
        this.users = new ArrayList<>(store.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /** The registered user with this email (case-insensitive), if any. */
    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    /** The registered user with this email and password, if the credentials match. */
    public Optional<User> authenticate(String email, String password) {
        int index = indexOfCredentials(email, password);
        return index < 0 ? Optional.empty() : Optional.of(users.get(index));
    }

    /** The registered user with this DNI, if any. */
    public Optional<User> findByDni(String dni) {
        return users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    public Result create(String firstName, String surnames, String dni, String email,
                         String password, String phone, String card) {
        if (!UserValidator.isNonBlank(firstName)) {
            return Result.error("First name must not be empty.");
        }
        if (!UserValidator.isNonBlank(surnames)) {
            return Result.error("Surnames must not be empty.");
        }
        if (!UserValidator.isValidDni(dni)) {
            return Result.error("Invalid DNI: " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            return Result.error("Invalid email: " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            return Result.error("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol.");
        }
        if (!UserValidator.isNonBlank(phone)) {
            return Result.error("Telephone number must not be empty.");
        }
        if (!UserValidator.isValidCard(card)) {
            return Result.error("Invalid card number: it must have exactly 16 digits.");
        }
        for (User u : users) {
            if (u.getDni().equals(dni)) {
                return Result.error("A user with DNI " + dni + " already exists.");
            }
            if (u.getEmail().equalsIgnoreCase(email)) {
                return Result.error("A user with email " + email + " already exists.");
            }
        }
        List<User> updated = new ArrayList<>(users);
        updated.add(new User(firstName, surnames, dni, email, password, phone, card));
        return commit(updated, "User " + email + " created.");
    }

    public Result delete(String email, String password) {
        int index = indexOfCredentials(email, password);
        if (index < 0) {
            return Result.error("Unknown email or wrong password.");
        }
        List<User> updated = new ArrayList<>(users);
        User removed = updated.remove(index);
        return commit(updated, "User " + removed.getEmail() + " deleted.");
    }

    public Result update(String email, String password, UserField field, String newValue) {
        int index = indexOfCredentials(email, password);
        if (index < 0) {
            return Result.error("Unknown email or wrong password.");
        }
        String problem = validateUpdate(index, field, newValue);
        if (problem != null) {
            return Result.error(problem);
        }
        User original = users.get(index);
        User changed = new User(original);
        apply(changed, field, newValue);
        List<User> candidate = new ArrayList<>(users);
        candidate.set(index, changed);
        try {
            store.save(candidate);
        } catch (IOException e) {
            return Result.error("Could not save users: " + e.getMessage());
        }
        // Keep the same instance so every object referring to this user sees the change.
        apply(original, field, newValue);
        return Result.ok("User " + original.getEmail() + " updated (" + field + ").");
    }

    /** Error message for an invalid new value, or {@code null} when the value is acceptable. */
    private String validateUpdate(int index, UserField field, String newValue) {
        return switch (field) {
            case NAME -> UserValidator.isNonBlank(newValue) ? null : "First name must not be empty.";
            case SURNAME -> UserValidator.isNonBlank(newValue) ? null : "Surnames must not be empty.";
            case PHONE -> UserValidator.isNonBlank(newValue) ? null : "Telephone number must not be empty.";
            case PASSWORD -> UserValidator.isValidPassword(newValue) ? null
                    : "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol.";
            case CARD -> UserValidator.isValidCard(newValue) ? null
                    : "Invalid card number: it must have exactly 16 digits.";
            case EMAIL -> validateNewEmail(index, newValue);
        };
    }

    private String validateNewEmail(int index, String newValue) {
        if (!UserValidator.isValidEmail(newValue)) {
            return "Invalid email: " + newValue;
        }
        for (int i = 0; i < users.size(); i++) {
            if (i != index && users.get(i).getEmail().equalsIgnoreCase(newValue)) {
                return "A user with email " + newValue + " already exists.";
            }
        }
        return null;
    }

    private static void apply(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCardNumber(value);
        }
    }

    /** Index of the user with this email and password, or -1. */
    private int indexOfCredentials(String email, String password) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u.getPassword().equals(password) ? i : -1;
            }
        }
        return -1;
    }

    private Result commit(List<User> updated, String successMessage) {
        try {
            store.save(updated);
        } catch (IOException e) {
            return Result.error("Could not save users: " + e.getMessage());
        }
        users = updated;
        return Result.ok(successMessage);
    }
}
