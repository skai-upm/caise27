package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.repository.UserRepository;
import java.util.Collection;
import java.util.List;

/**
 * Loads and saves the complete set of groups. Members are resolved against the registered users.
 */
public interface GroupStorage {

    List<Group> load(UserRepository users);

    void save(Collection<Group> groups);

    /** Storage that keeps nothing (in-memory use). */
    static GroupStorage none() {
        return new GroupStorage() {
            @Override
            public List<Group> load(UserRepository users) {
                return List.of();
            }

            @Override
            public void save(Collection<Group> groups) {
                // nothing to persist
            }
        };
    }
}
