package es.upm.etsisi.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Stores and retrieves groups, resolving members by DNI. Implementations are chosen and injected at start-up.
 */
public interface GroupRepository {
    List<Group> load(Map<String, User> usersByDni) throws IOException;

    void save(List<Group> groups) throws IOException;

    /**
     * Problems found by the last load (skipped records).
     */
    List<String> getWarnings();
}
