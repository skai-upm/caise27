package es.upm.parque.business.security;

/**
 * One-way protection of passwords: only hashes are stored and passwords are checked against them.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String hash);

    /**
     * True if the value is a hash produced by this hasher rather than a plaintext password.
     */
    boolean isHash(String value);
}
