package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Persistent store of active users.
 */
public interface UserRepository {

    List<User> load();

    void save(List<User> users);
}
