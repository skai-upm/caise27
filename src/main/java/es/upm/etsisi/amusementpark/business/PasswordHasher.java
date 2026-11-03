package es.upm.etsisi.amusementpark.business;

/**
 * Protects passwords before they are stored and verifies them afterwards.
 */
public interface PasswordHasher {

    /** Returns the protected value to store for a plaintext password. */
    String hash(String password);

    /** Tells whether a plaintext password corresponds to a stored protected value. */
    boolean matches(String password, String storedHash);
}
