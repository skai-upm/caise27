package es.upm.parque.business.security;

/**
 * Turns plaintext passwords into stored hashes and checks candidates against them.
 */
public interface PasswordEncoder {

    String hash(String plaintext);

    boolean matches(String plaintext, String hash);

    /**
     * True if the value looks like a hash produced by this encoder (and not a plaintext password).
     */
    boolean isHash(String value);
}
