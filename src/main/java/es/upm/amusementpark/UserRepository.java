package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves users, identified by their email address.
 */
public class UserRepository {

    private final Map<String, User> users = new LinkedHashMap<>();

    public void save(User user) {
        users.put(user.getEmail(), user);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }

    public boolean delete(String email) {
        return users.remove(email) != null;
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
