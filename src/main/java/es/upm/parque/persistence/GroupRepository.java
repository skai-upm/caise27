package es.upm.parque.persistence;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;

/**
 * Storage of groups; members are resolved against the loaded users by DNI.
 */
public interface GroupRepository {

    List<Group> load(Function<String, User> userByDni) throws IOException;

    void saveAll(List<Group> groups) throws IOException;
}
