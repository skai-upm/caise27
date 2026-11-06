package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.model.User;
import es.upm.park.persistence.UserFileStore;

/**
 * Keeps the users known to the application, optionally backed by a file
 * store that is loaded on creation and saved after every change.
 */
public class UserRepository {

    private final List<User> users = new ArrayList<>();
    private final UserFileStore store;

    /** In-memory repository without persistence. */
    public UserRepository() {
        this.store = null;
    }

    /** Repository loaded from, and saved to, the given store. */
    public UserRepository(UserFileStore store) {
        this.store = store;
        users.addAll(store.load());
    }

    /**
     * Adds a user. When persistent, the file is written first so that a
     * failed write leaves the repository unchanged.
     */
    public void add(User user) {
        List<User> updated = new ArrayList<>(users);
        updated.add(user);
        persist(updated);
        users.add(user);
    }

    /**
     * Removes a user. When persistent, the file is written first so that a
     * failed write leaves the repository unchanged.
     */
    public void remove(User user) {
        List<User> updated = new ArrayList<>(users);
        updated.remove(user);
        persist(updated);
        users.remove(user);
    }

    /**
     * Replaces a stored user by its updated copy. When persistent, the file is
     * written first so that a failed write leaves the repository unchanged.
     */
    public void replace(User old, User updated) {
        List<User> snapshot = new ArrayList<>(users);
        snapshot.set(snapshot.indexOf(old), updated);
        persist(snapshot);
        users.set(users.indexOf(old), updated);
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public Optional<User> findByDni(String dni) {
        return users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    private void persist(List<User> snapshot) {
        if (store != null) {
            store.save(snapshot);
        }
    }
}
