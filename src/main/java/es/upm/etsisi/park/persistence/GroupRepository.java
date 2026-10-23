package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.user.User;

/** Storage of groups; members are resolved against the active users by DNI. */
public interface GroupRepository {

    List<Group> load(Map<String, User> usersByDni) throws IOException;

    void save(List<Group> groups) throws IOException;
}
