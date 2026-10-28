package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.util.List;

import es.upm.amusementpark.model.User;

/**
 * Durable storage of the users of the system.
 */
public interface UserStorage {

    /**
     * Loads the stored users. Unreadable entries are skipped and described in {@code warnings}.
     */
    List<User> load(List<String> warnings) throws IOException;

    /**
     * Replaces the stored users with the given ones.
     */
    void save(List<User> users) throws IOException;
}
