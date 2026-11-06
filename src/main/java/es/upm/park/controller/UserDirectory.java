package es.upm.park.controller;

import java.util.Optional;

/**
 * What the central park management needs to know about registered users,
 * identified by email: whether they exist and their DNI.
 */
@FunctionalInterface
public interface UserDirectory {

    /** The DNI of the registered user with this email, or empty if there is none. */
    Optional<String> dniOf(String email);

    default boolean exists(String email) {
        return dniOf(email).isPresent();
    }
}
