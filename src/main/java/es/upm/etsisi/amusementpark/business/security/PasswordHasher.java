package es.upm.etsisi.amusementpark.business.security;

/**
 * Protects passwords before storage and verifies candidates against the stored value.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String storedHash);
}
