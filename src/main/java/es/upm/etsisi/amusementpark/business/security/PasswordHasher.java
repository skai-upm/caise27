package es.upm.etsisi.amusementpark.business.security;

/**
 * Protects passwords before storage and verifies candidates against the protected value.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String hash);
}
