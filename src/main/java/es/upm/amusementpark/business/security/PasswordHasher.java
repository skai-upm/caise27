package es.upm.amusementpark.business.security;

/**
 * Protects passwords so that only a one-way hash is ever stored.
 */
public interface PasswordHasher {

    /** Returns a new salted hash of {@code plainPassword}. */
    String hash(String plainPassword);

    /** Whether {@code plainPassword} corresponds to {@code storedHash}. */
    boolean matches(String plainPassword, String storedHash);
}
