package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;
import java.util.function.Function;

/**
 * Persistent store of groups and their members.
 */
public interface GroupRepository {

    /**
     * Loads groups, resolving members by DNI among the active users.
     */
    List<Group> load(Function<String, User> userByDni);

    void save(List<Group> groups);
}
