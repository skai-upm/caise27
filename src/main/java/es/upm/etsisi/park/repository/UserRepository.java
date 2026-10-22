package es.upm.etsisi.park.repository;

import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.UserFileStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * In-memory collection of users, persisted through a {@link UserFileStore}.
 */
public class UserRepository {

    private final UserFileStore store;
    private final List<User> users;

    public UserRepository(UserFileStore store) {
        this.store = store;
        this.users = new ArrayList<>(store.load());
    }

    public Optional<User> findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByDni(String dni) {
        for (User user : users) {
            if (user.getDni().equalsIgnoreCase(dni)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    public void add(User user) {
        users.add(user);
        save();
    }

    public void remove(User user) {
        users.remove(user);
        save();
    }

    /** Persists the current state, e.g. after a stored user has been modified. */
    public void save() {
        store.save(users);
    }
}
