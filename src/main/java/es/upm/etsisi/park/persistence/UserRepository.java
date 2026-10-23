package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.util.List;

import es.upm.etsisi.park.business.model.user.User;

/** Storage of registered users. */
public interface UserRepository {

    List<User> load() throws IOException;

    void save(List<User> users) throws IOException;
}
