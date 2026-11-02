package es.upm.etsisi.amusementpark.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.persistence.UserStorage;

/**
 * Registered users and the operations on them. Every mutation is validated and
 * saved before it is applied in memory, so a rejected operation changes nothing.
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

    public void create(User user) throws UserOperationException {
        if (!UserValidator.isValidDni(user.getDni())) {
            throw new UserOperationException("invalid DNI");
        }
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validateCard(user.getCard());
        if (findByDni(user.getDni()) != null) {
            throw new UserOperationException("a user with DNI " + user.getDni() + " already exists");
        }
        if (findByEmail(user.getEmail()) != null) {
            throw new UserOperationException("a user with email " + user.getEmail() + " already exists");
        }
        List<User> candidate = new ArrayList<>(users);
        candidate.add(user);
        commit(candidate);
    }

    public void delete(String email, String password) throws UserOperationException {
        User user = authenticate(email, password);
        List<User> candidate = new ArrayList<>(users);
        candidate.remove(user);
        commit(candidate);
    }

    public void update(String email, String password, UserField field, String value)
            throws UserOperationException {
        User current = authenticate(email, password);
        User updated = current.copy();
        switch (field) {
            case NAME -> updated.setFirstName(value);
            case SURNAME -> updated.setSurnames(value);
            case EMAIL -> {
                validateEmail(value);
                User owner = findByEmail(value);
                if (owner != null && owner != current) {
                    throw new UserOperationException("a user with email " + value + " already exists");
                }
                updated.setEmail(value);
            }
            case PASSWORD -> {
                validatePassword(value);
                updated.setPassword(value);
            }
            case PHONE -> updated.setPhone(value);
            case CARD -> {
                validateCard(value);
                updated.setCard(value);
            }
        }
        List<User> candidate = new ArrayList<>(users);
        candidate.set(candidate.indexOf(current), updated);
        commit(candidate);
    }

    private User authenticate(String email, String password) throws UserOperationException {
        User user = findByEmail(email);
        if (user == null) {
            throw new UserOperationException("no user with email " + email);
        }
        if (!user.hasPassword(password)) {
            throw new UserOperationException("incorrect password");
        }
        return user;
    }

    private void commit(List<User> candidate) throws UserOperationException {
        try {
            storage.save(candidate);
        } catch (IOException e) {
            throw new UserOperationException("could not save users: " + e.getMessage());
        }
        users = candidate;
    }

    private static void validateEmail(String email) throws UserOperationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("invalid email");
        }
    }

    private static void validatePassword(String password) throws UserOperationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException("invalid password");
        }
    }

    private static void validateCard(String card) throws UserOperationException {
        if (!UserValidator.isValidCard(card)) {
            throw new UserOperationException("invalid card number");
        }
    }
}
