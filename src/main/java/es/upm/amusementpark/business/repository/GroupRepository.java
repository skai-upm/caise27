package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** Storage of groups; members are resolved to registered users when loading. */
public interface GroupRepository {

    List<Group> loadAll(Function<String, User> userByDni) throws IOException;

    void saveAll(Collection<Group> groups) throws IOException;
}
