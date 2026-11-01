package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

/**
 * Storage of groups; members are resolved with the given user lookup by DNI.
 */
public interface GroupRepository {

    List<Group> load(Function<String, User> userByDni) throws IOException;

    void save(List<Group> groups) throws IOException;
}
