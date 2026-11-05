package es.upm.amusementpark.park;

/**
 * Lookup of registered users by e-mail, provided by user management.
 */
@FunctionalInterface
public interface UserDirectory {

    /** Directory used while no user management is plugged in: no user exists. */
    UserDirectory EMPTY = email -> false;

    boolean exists(String email);
}
