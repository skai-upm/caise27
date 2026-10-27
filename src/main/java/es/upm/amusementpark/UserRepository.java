package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves users, identified by their email address. When a
 * {@link UserFileStorage} is given, users are loaded from it on creation and
 * saved to it after every change.
 */
public class UserRepository {

    private final Map<String, User> users = new LinkedHashMap<>();
    private final UserFileStorage storage;

    /** Creates a repository that keeps users in memory only. */
    public UserRepository() {
        this.storage = null;
    }

    public UserRepository(UserFileStorage storage) {
        this.storage = storage;
        for (User user : storage.load()) {
            users.put(user.getEmail(), user);
        }
    }

    public void save(User user) {
        users.put(user.getEmail(), user);
        persist();
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }

    public boolean existsByDni(String dni) {
        for (User user : users.values()) {
            if (user.getDni().equals(dni)) {
                return true;
            }
        }
        return false;
    }

    public boolean delete(String email) {
        boolean removed = users.remove(email) != null;
        if (removed) {
            persist();
        }
        return removed;
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    private void persist() {
        if (storage != null) {
            storage.save(users.values());
        }
    }
}
