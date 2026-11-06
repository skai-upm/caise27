package es.upm.park.business.service;

/**
 * Protects passwords before storage and verifies entered passwords against
 * stored protected values.
 */
public interface PasswordHasher {

    /** Protected value to store for a (validated) password. */
    String hash(String plain);

    /** Whether the entered password corresponds to the stored value. */
    boolean matches(String plain, String stored);
}
