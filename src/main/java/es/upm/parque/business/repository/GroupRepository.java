package es.upm.parque.business.repository;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of groups; members are relinked to the loaded users through {@code userByDni}.
 */
public interface GroupRepository {

    List<Group> load(Function<String, User> userByDni);

    void save(List<Group> groups);
}
