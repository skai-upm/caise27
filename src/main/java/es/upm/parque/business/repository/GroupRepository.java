package es.upm.parque.business.repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

/**
 * Storage of groups. Members are linked to existing users through the given lookup.
 */
public interface GroupRepository {

    List<Group> load(Function<String, User> userByDni);

    void save(Collection<Group> groups);
}
