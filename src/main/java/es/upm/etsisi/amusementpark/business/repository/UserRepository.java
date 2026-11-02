package es.upm.etsisi.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;

import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Stores and retrieves users. Implementations are chosen and injected at start-up.
 */
public interface UserRepository {
    List<User> load() throws IOException;

    void save(List<User> users) throws IOException;

    /**
     * Problems found by the last load (skipped records).
     */
    List<String> getWarnings();
}
