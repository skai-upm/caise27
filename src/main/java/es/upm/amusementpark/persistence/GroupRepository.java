package es.upm.amusementpark.persistence;

import java.util.Collection;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

/**
 * Stores and retrieves groups and their members.
 */
public interface GroupRepository {

    /** Loads groups by identifier, resolving members among the given active users. */
    Map<String, Group> load(Map<String, User> usersByDni);

    void save(Collection<Group> groups);
}
