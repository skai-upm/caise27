package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.model.User;

/**
 * Keeps the users known to the application.
 */
public class UserRepository {

    private final List<User> users = new ArrayList<>();

    public void add(User user) {
        users.add(user);
    }

    public Optional<User> findByDni(String dni) {
        return users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    public boolean existsByEmail(String email) {
        return users.stream().anyMatch(u -> u.getEmail().equals(email));
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }
}
