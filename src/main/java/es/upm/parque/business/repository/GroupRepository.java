package es.upm.parque.business.repository;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Storage of groups; members are resolved through a DNI lookup.
 */
public interface GroupRepository {

    List<Group> load(Function<String, User> usersByDni);

    void saveAll(Collection<Group> groups);
}
