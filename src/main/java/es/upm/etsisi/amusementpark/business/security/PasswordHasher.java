package es.upm.etsisi.amusementpark.business.security;

/** Turns passwords into storable hashes and checks passwords against them. */
public interface PasswordHasher {

    /** Returns a salted hash of {@code plainPassword}. */
    String hash(String plainPassword);

    /** True if {@code plainPassword} corresponds to {@code storedHash}; false for anything else. */
    boolean matches(String plainPassword, String storedHash);
}
