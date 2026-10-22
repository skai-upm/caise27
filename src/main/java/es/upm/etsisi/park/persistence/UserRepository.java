package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.User;

import java.util.Optional;

/**
 * Registered users. {@link #findById(String)} looks users up by DNI.
 */
public interface UserRepository extends Repository<User> {

    /** Case-insensitive email lookup. */
    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);
}
