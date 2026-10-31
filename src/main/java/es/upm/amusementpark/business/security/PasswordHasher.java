package es.upm.amusementpark.business.security;

/** Protects passwords before they are stored and checks supplied passwords against them. */
public interface PasswordHasher {

    /** Returns the protected value to store for a raw password. */
    String hash(String rawPassword);

    /** Whether the raw password corresponds to the stored value. */
    boolean matches(String rawPassword, String storedValue);
}
