package es.upm.etsisi.amusementpark.user;

import es.upm.etsisi.amusementpark.persistence.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * User management operations. Every operation validates first and only replaces the
 * in-memory state once the new state has been persisted, so rejected or failed
 * operations leave both memory and storage unchanged.
 */
public class UserService {

    private final UserRepository repository;
    private List<User> users;

    public UserService(UserRepository repository) throws IOException {
        this.repository = repository;
        this.users = new ArrayList<>(repository.loadAll());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public void create(User user) throws UserException {
        if (!UserValidator.isValidDni(user.getDni())) {
            throw new UserException("invalid DNI");
        }
        checkEmail(user.getEmail(), null);
        checkPassword(user.getPassword());
        checkCard(user.getCard());
        if (users.stream().anyMatch(u -> u.getDni().equals(user.getDni()))) {
            throw new UserException("DNI already registered");
        }
        List<User> updated = new ArrayList<>(users);
        updated.add(new User(user));
        commit(updated);
    }

    public void delete(String email, String password) throws UserException {
        User existing = authenticate(email, password);
        List<User> updated = new ArrayList<>(users);
        updated.remove(existing);
        commit(updated);
    }

    public void update(String email, String password, UserField field, String newValue)
            throws UserException {
        User existing = authenticate(email, password);
        User changed = new User(existing);
        switch (field) {
            case NAME -> changed.setFirstName(newValue);
            case SURNAME -> changed.setSurnames(newValue);
            case EMAIL -> {
                checkEmail(newValue, existing);
                changed.setEmail(newValue);
            }
            case PASSWORD -> {
                checkPassword(newValue);
                changed.setPassword(newValue);
            }
            case PHONE -> changed.setPhone(newValue);
            case CARD -> {
                checkCard(newValue);
                changed.setCard(newValue);
            }
        }
        List<User> updated = new ArrayList<>(users);
        updated.set(updated.indexOf(existing), changed);
        commit(updated);
    }

    private User authenticate(String email, String password) throws UserException {
        User user = findByEmail(email).orElseThrow(() -> new UserException("user not found"));
        if (!user.hasPassword(password)) {
            throw new UserException("incorrect password");
        }
        return user;
    }

    private void checkEmail(String email, User owner) throws UserException {
        if (!UserValidator.isValidEmail(email)) {
            throw new UserException("invalid email");
        }
        Optional<User> holder = findByEmail(email);
        if (holder.isPresent() && holder.get() != owner) {
            throw new UserException("email already registered");
        }
    }

    private static void checkPassword(String password) throws UserException {
        if (!UserValidator.isValidPassword(password)) {
            throw new UserException("invalid password");
        }
    }

    private static void checkCard(String card) throws UserException {
        if (!UserValidator.isValidCard(card)) {
            throw new UserException("invalid card number");
        }
    }

    private void commit(List<User> updated) throws UserException {
        try {
            repository.saveAll(updated);
        } catch (IOException e) {
            throw new UserException("could not save users: " + e.getMessage(), e);
        }
        users = updated;
    }
}
