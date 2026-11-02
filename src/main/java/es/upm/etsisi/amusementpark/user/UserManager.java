package es.upm.etsisi.amusementpark.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.OperationException;
import es.upm.etsisi.amusementpark.persistence.UserStorage;

/**
 * Registered users and the operations on them. Every mutation is validated and
 * saved before it is applied in memory, so a rejected operation changes nothing.
 * Updates modify the existing {@link User} object, so references held elsewhere
 * (groups, tickets, attractions) stay valid.
 */
public class UserManager {
    private final UserStorage storage;
    private List<User> users;

    public UserManager(UserStorage storage) throws IOException {
        this.storage = storage;
        this.users = new ArrayList<>(storage.load());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public boolean isRegistered(User user) {
        return users.stream().anyMatch(u -> u == user);
    }

    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private User findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    public void create(User user) throws OperationException {
        if (!UserValidator.isValidDni(user.getDni())) {
            throw new OperationException("invalid DNI");
        }
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validateCard(user.getCard());
        if (findByDni(user.getDni()) != null) {
            throw new OperationException("a user with DNI " + user.getDni() + " already exists");
        }
        if (findByEmail(user.getEmail()) != null) {
            throw new OperationException("a user with email " + user.getEmail() + " already exists");
        }
        List<User> candidate = new ArrayList<>(users);
        candidate.add(user);
        commit(candidate);
    }

    /**
     * Returns the user with this email if the password matches.
     */
    public User authenticate(String email, String password) throws OperationException {
        User user = findByEmail(email);
        if (user == null) {
            throw new OperationException("no user with email " + email);
        }
        if (!user.hasPassword(password)) {
            throw new OperationException("incorrect password");
        }
        return user;
    }

    public void remove(User user) throws OperationException {
        List<User> candidate = new ArrayList<>(users);
        candidate.removeIf(u -> u == user);
        commit(candidate);
    }

    public void update(String email, String password, UserField field, String value)
            throws OperationException {
        User current = authenticate(email, password);
        switch (field) {
            case EMAIL -> {
                validateEmail(value);
                User owner = findByEmail(value);
                if (owner != null && owner != current) {
                    throw new OperationException("a user with email " + value + " already exists");
                }
            }
            case PASSWORD -> validatePassword(value);
            case CARD -> validateCard(value);
            default -> {
                // NAME, SURNAME and PHONE have no format rules
            }
        }
        User updated = current.copy();
        apply(updated, field, value);
        List<User> candidate = new ArrayList<>(users);
        candidate.set(candidate.indexOf(current), updated);
        save(candidate);
        apply(current, field, value);
    }

    private static void apply(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private void commit(List<User> candidate) throws OperationException {
        save(candidate);
        users = candidate;
    }

    private void save(List<User> candidate) throws OperationException {
        try {
            storage.save(candidate);
        } catch (IOException e) {
            throw new OperationException("could not save users: " + e.getMessage());
        }
    }

    private static void validateEmail(String email) throws OperationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new OperationException("invalid email");
        }
    }

    private static void validatePassword(String password) throws OperationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new OperationException("invalid password");
        }
    }

    private static void validateCard(String card) throws OperationException {
        if (!UserValidator.isValidCard(card)) {
            throw new OperationException("invalid card number");
        }
    }
}
