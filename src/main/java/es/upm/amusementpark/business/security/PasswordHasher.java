package es.upm.amusementpark.business.security;

/**
 * Protects passwords so that only a derived value is ever stored.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String storedHash);
}
