package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.User;

/**
 * Stores and retrieves the registered users, indexed by DNI.
 */
public class UserRepository {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    public void add(User user) {
        usersByDni.put(user.getDni(), user);
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
