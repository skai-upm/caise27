package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
     * Changes a stored user in place. When persistent, the change is first
     * applied to a copy and written, so a failed write leaves the user
     * unchanged; the same object keeps representing the user.
     */
    public void update(User user, Consumer<User> change) {
        User changed = user.copy();
        change.accept(changed);
        List<User> snapshot = new ArrayList<>(users);
        snapshot.set(snapshot.indexOf(user), changed);
        persist(snapshot);
        change.accept(user);
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
