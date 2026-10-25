package es.upm.etsisi.park.business.security;

/**
 * One-way password protection used before storing and when verifying passwords.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String storedHash);
}
