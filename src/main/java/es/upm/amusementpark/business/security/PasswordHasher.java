package es.upm.amusementpark.business.security;

/**
 * Protects passwords before they are stored and verifies supplied passwords against stored values.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedValue);
}
