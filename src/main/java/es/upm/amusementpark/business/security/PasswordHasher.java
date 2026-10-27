package es.upm.amusementpark.business.security;

/**
 * Protects passwords for storage and verifies entered passwords against stored values.
 */
public interface PasswordHasher {

    /** Returns a newly generated protected value for the plain password. */
    String hash(String plain);

    /** Whether the plain password corresponds to the stored protected value. */
    boolean verify(String plain, String stored);
}
