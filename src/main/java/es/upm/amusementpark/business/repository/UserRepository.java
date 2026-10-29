package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.User;

import java.util.Collection;
import java.util.List;

/** Replaceable storage of the park's users. */
public interface UserRepository {

    void saveAll(Collection<User> users);

    /** @param skipped receives stored entries that could not be read */
    List<User> loadAll(List<String> skipped);
}
