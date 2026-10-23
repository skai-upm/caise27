package es.upm.park.persistence;

import es.upm.park.business.model.User;

import java.util.List;

/**
 * Storage of registered users.
 */
public interface UserRepository {

    List<User> loadAll();

    void saveAll(List<User> users);
}
