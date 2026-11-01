package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;

import es.upm.amusementpark.business.model.User;

/**
 * Storage of registered users.
 */
public interface UserRepository {

    List<User> load() throws IOException;

    void save(List<User> users) throws IOException;
}
