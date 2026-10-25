package es.upm.parque.business.security;

/**
 * One-way protection of passwords.
 */
public interface PasswordHasher {

    String hash(String plaintext);

    boolean matches(String plaintext, String hash);

    /** Whether the stored value is already a hash produced by this hasher. */
    boolean isHash(String stored);
}
