package es.upm.park.persistence;

import es.upm.park.business.model.User;
import java.io.IOException;
import java.util.List;

/** Storage of registered users. */
public interface UserRepository {

    List<User> load() throws IOException;

    void save(List<User> users) throws IOException;
}
