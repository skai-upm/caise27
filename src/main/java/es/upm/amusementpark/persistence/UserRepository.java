package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;
import java.util.List;

/**
 * Registered users. Implementations decide where and how they are stored.
 */
public interface UserRepository {

    List<User> findAll();

    User findByEmail(String email);

    User findByDni(String dni);

    /**
     * Whether the given user object is a currently registered user.
     */
    boolean contains(User user);

    void add(User user);

    void remove(User user);

    /**
     * Persists changes made to users already held by the repository.
     */
    void save();
}
