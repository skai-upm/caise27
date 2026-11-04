package es.upm.park.business.service;

/** Turns plaintext passwords into stored hashes and verifies passwords against them. */
public interface PasswordHasher {

    /** Returns a salted hash of {@code plaintext}. */
    String hash(String plaintext);

    /** True if {@code plaintext} corresponds to {@code storedHash}; false for values that are not hashes. */
    boolean matches(String plaintext, String storedHash);
}
