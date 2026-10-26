package es.upm.parque.business.security;

/**
 * Turns passwords into one-way hashes and verifies passwords against them.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String hash);

    /** @return true if {@code value} is a hash produced by this hasher (and not a plain-text password) */
    boolean isHash(String value);
}
