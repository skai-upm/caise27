package es.upm.parque.business.security;

/**
 * One-way password protection used to store and verify user passwords.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String storedHash);

    /**
     * True when the stored value already is a hash produced by this hasher.
     */
    boolean isHash(String storedValue);
}
