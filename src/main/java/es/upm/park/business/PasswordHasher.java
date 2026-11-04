package es.upm.park.business;

/**
 * Turns passwords into the value that is stored and checks supplied passwords against it.
 */
public interface PasswordHasher {

    /** Hash to store for a (validated) plaintext password. */
    String hash(String plainPassword);

    /** Whether the plaintext password corresponds to the stored hash; false for invalid stored values. */
    boolean matches(String plainPassword, String storedHash);
}
