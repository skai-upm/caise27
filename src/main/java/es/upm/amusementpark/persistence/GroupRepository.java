package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores and retrieves the full set of groups.
 */
public interface GroupRepository {

    /** Loads all groups, resolving members by DNI. */
    List<Group> loadAll(Function<String, User> userByDni) throws IOException;

    void saveAll(Collection<Group> groups) throws IOException;
}
