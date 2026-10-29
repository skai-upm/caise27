package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Stores and retrieves active users.
 */
public interface UserRepository {

    List<User> load() throws IOException;

    void saveAll(Collection<User> users) throws IOException;
}
