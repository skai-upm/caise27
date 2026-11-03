package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.group.Group;

import java.util.List;
import java.util.Map;

/**
 * Stores and retrieves groups, linking members to already loaded users.
 */
public interface GroupRepository {

    List<Group> load(Map<String, User> usersByDni) throws StorageException;

    void save(List<Group> groups) throws StorageException;
}
