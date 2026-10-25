package es.upm.parque.persistence;

import java.io.IOException;
import java.util.List;

import es.upm.parque.business.model.User;

/**
 * Storage of users.
 */
public interface UserRepository {

    List<User> load() throws IOException;

    void saveAll(List<User> users) throws IOException;
}
