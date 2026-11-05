package es.upm.parque.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.model.User;
import es.upm.parque.persistence.UserFileStore;

/**
 * Stores and retrieves users. Users are kept in insertion order and indexed by DNI.
 */
public class UserRepository {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();
    private final UserFileStore store;

    /** Creates a purely in-memory repository. */
    public UserRepository() {
        this.store = null;
    }

    /** Creates a repository backed by a file: users are loaded now and saved after each change. */
    public UserRepository(UserFileStore store) {
        this.store = store;
        for (User user : store.load()) {
            usersByDni.put(user.getDni(), user);
        }
    }

    public void add(User user) {
        usersByDni.put(user.getDni(), user);
        persist();
    }

    /** Removes the user with the given DNI, if present, and persists the change. */
    public void remove(String dni) {
        if (usersByDni.remove(dni) != null) {
            persist();
        }
    }

    public User findByEmail(String email) {
        for (User user : usersByDni.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private void persist() {
        if (store != null) {
            store.save(findAll());
        }
    }

    public User findByDni(String dni) {
        return usersByDni.get(dni);
    }

    public boolean existsByDni(String dni) {
        return usersByDni.containsKey(dni);
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByDni.values());
    }

    public int size() {
        return usersByDni.size();
    }
}
