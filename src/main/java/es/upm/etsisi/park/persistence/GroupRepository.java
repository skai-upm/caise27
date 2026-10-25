package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Storage for groups and their members.
 */
public interface GroupRepository {

    List<Group> loadAll(Function<String, Optional<User>> userByDni);

    void saveAll(Collection<Group> groups);
}
