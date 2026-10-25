package es.upm.parque.business.repository;

import es.upm.parque.business.model.User;

import java.util.Collection;
import java.util.List;

/**
 * Storage of users.
 */
public interface UserRepository {

    List<User> load();

    void saveAll(Collection<User> users);
}
