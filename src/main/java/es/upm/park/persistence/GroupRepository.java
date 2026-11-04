package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Storage of groups; members are linked to registered users on load. */
public interface GroupRepository {

    List<Group> load(Function<String, Optional<User>> userByDni) throws IOException;

    void save(List<Group> groups) throws IOException;
}
