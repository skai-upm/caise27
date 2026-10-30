package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import java.util.List;

/**
 * User groups and their members.
 */
public interface GroupRepository {

    List<Group> findAll();

    Group findById(String id);

    List<Group> groupsOf(User user);

    void add(Group group);

    void remove(Group group);

    /**
     * Persists changes made to groups already held by the repository.
     */
    void save();
}
