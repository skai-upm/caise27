package es.upm.etsisi.amusementpark.business.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;

/**
 * Registered users and the operations on them. Every mutation is validated and
 * saved before it is applied in memory, so a rejected operation changes nothing.
 * Updates modify the existing {@link User} object, so references held elsewhere
 * (groups, tickets, attractions) stay valid. Passwords are stored as BCrypt hashes.
 */
public class UserManager {
    private final UserRepository storage;
    private List<User> users;

    public UserManager(UserRepository storage) throws IOException {
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

    /**
     * Registers a user from a candidate carrying the plaintext password, which is
     * validated and replaced by its BCrypt hash before anything is stored.
     */
    public void create(User user) throws ParkException {
        if (!UserValidator.isValidDni(user.getDni())) {
            throw new InvalidDataException("invalid DNI");
        }
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validateCard(user.getCard());
        if (findByDni(user.getDni()) != null) {
            throw new RuleViolationException("a user with DNI " + user.getDni() + " already exists");
        }
        if (findByEmail(user.getEmail()) != null) {
            throw new RuleViolationException("a user with email " + user.getEmail() + " already exists");
        }
        User registered = new User(user.getFirstName(), user.getSurnames(), user.getDni(), user.getEmail(),
                Passwords.hash(user.getPassword()), user.getPhone(), user.getCard());
        List<User> candidate = new ArrayList<>(users);
        candidate.add(registered);
        commit(candidate);
    }

    /**
     * Returns the user with this email if the password matches.
     */
    public User authenticate(String email, String password) throws ParkException {
        User user = findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        if (!user.hasPassword(password)) {
            throw new RuleViolationException("incorrect password");
        }
        return user;
    }

    public void remove(User user) throws ParkException {
        List<User> candidate = new ArrayList<>(users);
        candidate.removeIf(u -> u == user);
        commit(candidate);
    }

    public void update(String email, String password, UserField field, String value)
            throws ParkException {
        User current = authenticate(email, password);
        switch (field) {
            case EMAIL -> {
                validateEmail(value);
                User owner = findByEmail(value);
                if (owner != null && owner != current) {
                    throw new RuleViolationException("a user with email " + value + " already exists");
                }
            }
            case PASSWORD -> validatePassword(value);
            case CARD -> validateCard(value);
            default -> {
                // NAME, SURNAME and PHONE have no format rules
            }
        }
        String stored = field == UserField.PASSWORD ? Passwords.hash(value) : value;
        User updated = current.copy();
        apply(updated, field, stored);
        List<User> candidate = new ArrayList<>(users);
        candidate.set(candidate.indexOf(current), updated);
        save(candidate);
        apply(current, field, stored);
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

    private void commit(List<User> candidate) throws ParkException {
        save(candidate);
        users = candidate;
    }

    private void save(List<User> candidate) throws ParkException {
        try {
            storage.save(candidate);
        } catch (IOException e) {
            throw new StorageException("could not save users: " + e.getMessage());
        }
    }

    private static void validateEmail(String email) throws ParkException {
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email");
        }
    }

    private static void validatePassword(String password) throws ParkException {
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException("invalid password");
        }
    }

    private static void validateCard(String card) throws ParkException {
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("invalid card number");
        }
    }
}
