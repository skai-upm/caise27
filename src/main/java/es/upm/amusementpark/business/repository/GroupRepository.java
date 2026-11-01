package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

/**
 * Storage of groups; members are resolved against the already loaded users, keyed by DNI.
 */
public interface GroupRepository {

    List<Group> load(Map<String, User> usersByDni) throws IOException;

    void save(List<Group> groups) throws IOException;
}
