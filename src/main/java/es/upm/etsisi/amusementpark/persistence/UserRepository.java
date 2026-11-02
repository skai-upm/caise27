package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.user.User;
import java.io.IOException;
import java.util.List;

/**
 * Stores and retrieves users.
 */
public interface UserRepository {

    List<User> loadAll() throws IOException;

    void saveAll(List<User> users) throws IOException;
}
