package es.upm.etsisi.park.business;

/**
 * Strategy for protecting stored passwords.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String storedHash);
}
