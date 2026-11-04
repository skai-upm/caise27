package es.upm.park;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        int index = authenticate(email, password);
        if (index < 0) {
            return Result.error("Unknown email or wrong password.");
        }
        List<User> updated = new ArrayList<>(users);
        User removed = updated.remove(index);
        return commit(updated, "User " + removed.getEmail() + " deleted.");
    }

    public Result update(String email, String password, UserField field, String newValue) {
        int index = authenticate(email, password);
        if (index < 0) {
            return Result.error("Unknown email or wrong password.");
        }
        User changed = new User(users.get(index));
        switch (field) {
            case NAME -> {
                if (!UserValidator.isNonBlank(newValue)) {
                    return Result.error("First name must not be empty.");
                }
                changed.setFirstName(newValue);
            }
            case SURNAME -> {
                if (!UserValidator.isNonBlank(newValue)) {
                    return Result.error("Surnames must not be empty.");
                }
                changed.setSurnames(newValue);
            }
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    return Result.error("Invalid email: " + newValue);
                }
                for (int i = 0; i < users.size(); i++) {
                    if (i != index && users.get(i).getEmail().equalsIgnoreCase(newValue)) {
                        return Result.error("A user with email " + newValue + " already exists.");
                    }
                }
                changed.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    return Result.error("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol.");
                }
                changed.setPassword(newValue);
            }
            case PHONE -> {
                if (!UserValidator.isNonBlank(newValue)) {
                    return Result.error("Telephone number must not be empty.");
                }
                changed.setPhone(newValue);
            }
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    return Result.error("Invalid card number: it must have exactly 16 digits.");
                }
                changed.setCardNumber(newValue);
            }
        }
        List<User> updated = new ArrayList<>(users);
        updated.set(index, changed);
        return commit(updated, "User " + changed.getEmail() + " updated (" + field + ").");
    }

    /** Index of the user with this email and password, or -1. */
    private int authenticate(String email, String password) {
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
