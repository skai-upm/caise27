package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** Storage of registered users. */
public interface UserRepository {

    List<User> loadAll() throws IOException;

    void saveAll(Collection<User> users) throws IOException;
}
