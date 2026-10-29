package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores and retrieves groups; members are resolved against the given active users by DNI.
 */
public interface GroupRepository {

    List<Group> load(Map<String, User> usersByDni) throws IOException;

    void saveAll(Collection<Group> groups) throws IOException;
}
