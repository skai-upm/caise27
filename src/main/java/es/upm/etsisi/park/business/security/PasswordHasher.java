package es.upm.etsisi.park.business.security;

/**
 * Protects passwords before they are stored and checks candidates against stored values.
 */
public interface PasswordHasher {

    String hash(String password);

    /** False when the candidate does not match or the stored value is not a valid hash. */
    boolean matches(String candidate, String storedHash);
}
