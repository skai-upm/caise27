package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.User;
import java.util.Collection;
import java.util.List;

/**
 * Storage for users that survives application runs.
 */
public interface UserRepository {

    List<User> loadAll();

    void saveAll(Collection<User> users);
}
