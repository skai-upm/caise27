package es.upm.parque.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.model.User;

/**
 * Stores and retrieves users. Users are kept in insertion order and indexed by DNI.
 */
public class UserRepository {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    public void add(User user) {
        usersByDni.put(user.getDni(), user);
    }

    public User findByDni(String dni) {
        return usersByDni.get(dni);
    }

    public boolean existsByDni(String dni) {
        return usersByDni.containsKey(dni);
    }

    public boolean existsByEmail(String email) {
        for (User user : usersByDni.values()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByDni.values());
    }

    public int size() {
        return usersByDni.size();
    }
}
