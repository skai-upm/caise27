package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores and restores user groups, linking members to the registered users.
 */
public interface GroupRepository {

    /**
     * @param userByDni resolves a stored DNI to the registered user, or null if it no longer exists
     */
    List<Group> load(Function<String, User> userByDni);

    void save(Collection<Group> groups);
}
