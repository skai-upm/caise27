package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of groups and their members.
 */
public interface GroupRepository {

    /**
     * @param userByDni resolves active users referenced by the stored groups
     */
    List<Group> loadAll(Function<String, User> userByDni);

    void saveAll(Iterable<Group> groups);
}
