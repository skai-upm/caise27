package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.User;
import java.util.List;

/**
 * Loads and saves the complete set of users.
 */
public interface UserStorage {

    List<User> load();

    void save(List<User> users);

    /** Storage that keeps nothing (in-memory use). */
    static UserStorage none() {
        return new UserStorage() {
            @Override
            public List<User> load() {
                return List.of();
            }

            @Override
            public void save(List<User> users) {
                // nothing to persist
            }
        };
    }
}
