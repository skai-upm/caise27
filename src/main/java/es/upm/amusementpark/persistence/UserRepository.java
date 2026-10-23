package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Stores and retrieves the full set of users.
 */
public interface UserRepository {

    List<User> loadAll() throws IOException;

    void saveAll(Collection<User> users) throws IOException;
}
