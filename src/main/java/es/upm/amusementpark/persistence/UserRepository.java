package es.upm.amusementpark.persistence;

import java.util.List;

import es.upm.amusementpark.business.model.User;

/**
 * Stores and retrieves the registered users.
 */
public interface UserRepository {

    List<User> load();

    void save(List<User> users);
}
