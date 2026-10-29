package es.upm.amusementpark.business.security;

/**
 * Protects passwords for storage and verifies entered passwords against the stored value.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String storedHash);
}
