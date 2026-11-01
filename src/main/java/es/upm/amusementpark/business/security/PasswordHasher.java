package es.upm.amusementpark.business.security;

/**
 * Protects passwords before they are stored and verifies supplied passwords
 * against the stored value.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String stored);
}
