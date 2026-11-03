package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;

import java.util.List;

/**
 * Stores and retrieves users.
 */
public interface UserRepository {

    List<User> load() throws StorageException;

    void save(List<User> users) throws StorageException;
}
