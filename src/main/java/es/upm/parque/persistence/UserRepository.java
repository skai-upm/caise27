package es.upm.parque.persistence;

import es.upm.parque.business.model.User;
import java.util.Collection;
import java.util.List;

/**
 * Stores and restores the registered users.
 */
public interface UserRepository {

    List<User> load();

    void save(Collection<User> users);
}
