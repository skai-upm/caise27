package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.UserFileStore;

/**
 * Stores and retrieves the registered users, indexed by DNI. When backed by a
 * {@link UserFileStore}, users are loaded on creation and saved after every change.
 */
public class UserRepository {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();
    private final UserFileStore store;

    /** In-memory repository without persistence. */
    public UserRepository() {
        this.store = null;
    }

    public UserRepository(UserFileStore store) {
        this.store = store;
        for (User user : store.load()) {
            usersByDni.put(user.getDni(), user);
        }
    }

    public void add(User user) {
        usersByDni.put(user.getDni(), user);
        save();
    }

    public void remove(User user) {
        usersByDni.remove(user.getDni());
        save();
    }

    private void save() {
        if (store != null) {
            store.save(usersByDni.values());
        }
    }

    public User findByDni(String dni) {
        return usersByDni.get(dni);
    }

    public User findByEmail(String email) {
        for (User user : usersByDni.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public boolean existsDni(String dni) {
        return usersByDni.containsKey(dni);
    }

    public boolean existsEmail(String email) {
        return findByEmail(email) != null;
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByDni.values());
    }

    public int size() {
        return usersByDni.size();
    }
}
