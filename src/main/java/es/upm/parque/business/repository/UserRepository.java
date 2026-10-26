package es.upm.parque.business.repository;

import es.upm.parque.business.model.User;

import java.util.List;

/**
 * Storage of registered users.
 */
public interface UserRepository {

    List<User> load();

    void save(List<User> users);
}
