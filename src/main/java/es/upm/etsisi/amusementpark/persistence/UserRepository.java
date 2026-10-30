package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves users, keeping the backing file in sync with every change.
 */
public class UserRepository {

    private final UserFileStore store;
    private final List<User> users;

    public UserRepository(UserFileStore store) {
        this.store = store;
        this.users = new ArrayList<>(store.load());
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public Optional<User> findByDni(String dni) {
        return users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    /**
     * Whether this exact user object is a registered (not deleted) user.
     */
    public boolean isRegistered(User user) {
        return users.contains(user);
    }

    public List<User> findAll() {
        return List.copyOf(users);
    }

    public void add(User user) {
        users.add(user);
        persist();
    }

    public void remove(User user) {
        users.remove(user);
        persist();
    }

    /**
     * Persists changes made to a user that is already stored.
     */
    public void update(User user) {
        persist();
    }

    private void persist() {
        store.save(users);
    }
}
