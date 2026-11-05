package es.upm.amusementpark.park;

/**
 * Lookup of registered users by e-mail, provided by user management.
 */
@FunctionalInterface
public interface UserDirectory {

    /** Directory used while no user management is plugged in: no user exists. */
    UserDirectory EMPTY = email -> false;

    boolean exists(String email);

    /**
     * @param email user e-mail
     * @return the user's DNI, or {@code null} if unknown
     */
    default String dniOf(String email) {
        return null;
    }
}
