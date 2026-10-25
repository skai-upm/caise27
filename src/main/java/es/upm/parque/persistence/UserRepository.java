package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import es.upm.parque.business.model.User;

/**
 * Storage abstraction for users. Implementations decide where and how data is kept.
 */
public interface UserRepository {

    List<User> load() throws IOException;

    void save(Collection<User> users) throws IOException;
}
