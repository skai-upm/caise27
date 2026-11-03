package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Repository of groups. Implementations decide where and how the data is stored; controllers depend only
 * on this interface, so the implementation or its location can be replaced at startup.
 */
public interface GroupRepository {

    /** Loads the stored groups, linking members to the given registered users. */
    List<Group> load(List<User> users);

    /** Stores exactly the given groups. */
    void save(List<Group> groups);
}
