package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.User;

import java.util.Collection;
import java.util.List;

/**
 * Persistent store of users.
 */
public interface UserRepository {

    List<User> load();

    void save(Collection<User> users);

    /** Problems found during the last load; problematic records are skipped. */
    List<String> getProblems();
}
