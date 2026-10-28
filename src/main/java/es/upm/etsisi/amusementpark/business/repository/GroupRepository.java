package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Persistent store of groups and their members.
 */
public interface GroupRepository {

    /**
     * @param usersByEmail resolves member e-mails among the active users
     */
    List<Group> load(Function<String, Optional<User>> usersByEmail);

    void save(Collection<Group> groups);

    List<String> getProblems();
}
