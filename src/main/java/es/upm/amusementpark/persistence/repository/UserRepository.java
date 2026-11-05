package es.upm.amusementpark.persistence.repository;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.storage.UserStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves users, keeping the configured storage up to date.
 */
public class UserRepository {

    private final List<User> users;
    private final UserStorage storage;

    public UserRepository() {
        this(UserStorage.none());
    }

    public UserRepository(UserStorage storage) {
        this.storage = storage;
        this.users = new ArrayList<>(storage.load());
    }

    public void add(User user) {
        users.add(user);
        storage.save(users);
    }

    public void remove(User user) {
        users.remove(user);
        storage.save(users);
    }

    /** Persists changes made to a user already stored in this repository. */
    public void update(User user) {
        if (users.contains(user)) {
            storage.save(users);
        }
    }

    public Optional<User> findByDni(String dni) {
        return users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    public int count() {
        return users.size();
    }
}
