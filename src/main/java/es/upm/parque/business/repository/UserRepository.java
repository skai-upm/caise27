package es.upm.parque.business.repository;

import java.util.List;

import es.upm.parque.business.model.User;

/**
 * Storage of users.
 */
public interface UserRepository {

    List<User> load();

    void save(List<User> users);
}
