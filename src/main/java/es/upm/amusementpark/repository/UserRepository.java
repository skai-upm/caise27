package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.amusementpark.model.User;

/**
 * Stores and retrieves users, keyed by DNI and kept in insertion order.
 */
public class UserRepository {

    private final Map<String, User> usersByDni = new LinkedHashMap<>();

    public void save(User user) {
        usersByDni.put(user.getDni(), user);
    }

    public void delete(String dni) {
        usersByDni.remove(dni);
    }

    public Optional<User> findByDni(String dni) {
        return Optional.ofNullable(usersByDni.get(dni));
    }

    public Optional<User> findByEmail(String email) {
        return usersByDni.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByDni.values());
    }
}
