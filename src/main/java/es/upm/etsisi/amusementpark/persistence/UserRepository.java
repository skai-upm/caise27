package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Repository of users. Implementations decide where and how the data is stored; controllers depend only
 * on this interface, so the implementation or its location can be replaced at startup.
 */
public interface UserRepository {

    /** Loads every stored user. */
    List<User> load();

    /** Stores exactly the given users. */
    void save(List<User> users);
}
