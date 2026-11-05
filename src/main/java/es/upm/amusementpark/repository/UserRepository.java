package es.upm.amusementpark.repository;

import es.upm.amusementpark.model.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves users.
 */
public class UserRepository {

    private final List<User> users = new ArrayList<>();

    public void add(User user) {
        users.add(user);
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
