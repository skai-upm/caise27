package es.upm.park.business.controller;

import es.upm.park.business.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves the users of the park (by email and by DNI), separately
 * from the user class and the application's main class. It answers the
 * {@link UserDirectory} questions of the central park management.
 */
public class UserRegistry implements UserDirectory {

    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    /** Replaces all users with the given ones (used when loading stored users). */
    public void restore(List<User> users) {
        usersByEmail.clear();
        users.forEach(this::save);
    }

    /** Stores a user, replacing any user with the same email. */
    public void save(User user) {
        usersByEmail.put(user.getEmail(), user);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    public Optional<User> findByDni(String dni) {
        return usersByEmail.values().stream().filter(user -> user.getDni().equals(dni)).findFirst();
    }

    public void remove(String email) {
        usersByEmail.remove(email);
    }

    public List<User> all() {
        return List.copyOf(usersByEmail.values());
    }

    @Override
    public Optional<String> dniOf(String email) {
        return findByEmail(email).map(User::getDni);
    }
}
