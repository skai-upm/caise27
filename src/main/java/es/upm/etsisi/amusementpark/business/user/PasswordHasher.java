package es.upm.etsisi.amusementpark.business.user;

/**
 * Protects passwords before they are stored and verifies candidates against stored values.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String storedHash);
}
