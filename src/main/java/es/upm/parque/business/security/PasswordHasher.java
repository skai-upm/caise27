package es.upm.parque.business.security;

/**
 * Turns passwords into hashes and checks passwords against stored hashes.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String hash);

    /**
     * Whether the stored value already is a hash produced by this hasher.
     */
    boolean isHash(String value);
}
