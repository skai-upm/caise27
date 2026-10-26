package es.upm.parque.business.security;

/**
 * One-way protection of passwords: only hashes are stored and supplied passwords are checked against them.
 */
public interface PasswordEncoder {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);

    /** Whether {@code value} is a hash produced by this encoder (as opposed to a plaintext password). */
    boolean isHash(String value);
}
