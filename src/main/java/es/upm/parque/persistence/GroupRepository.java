package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

/**
 * Storage abstraction for groups. Implementations decide where and how data is kept.
 */
public interface GroupRepository {

    /**
     * Loads the stored groups, linking each member DNI to an existing user through the given lookup.
     */
    List<Group> load(Function<String, User> userByDni) throws IOException;

    void save(Collection<Group> groups) throws IOException;
}
