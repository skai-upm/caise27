package es.upm.etsisi.amusementpark.business.security;

/** Protects passwords: only the value returned by {@link #hash} is ever stored. */
public interface PasswordHasher {

    /** Returns a salted hash of the plaintext password. */
    String hash(String plain);

    /** True if the plaintext password corresponds to the stored hash. */
    boolean matches(String plain, String storedHash);
}
