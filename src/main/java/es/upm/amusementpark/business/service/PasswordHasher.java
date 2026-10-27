package es.upm.amusementpark.business.service;

/**
 * Protects passwords before they are stored and verifies entered passwords
 * against stored values.
 */
public interface PasswordHasher {

    /** Returns a new protected value for the password. */
    String hash(String password);

    /** Whether the password corresponds to the stored protected value. */
    boolean matches(String password, String storedValue);
}
