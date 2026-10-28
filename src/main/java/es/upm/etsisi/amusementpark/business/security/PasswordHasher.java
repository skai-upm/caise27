package es.upm.etsisi.amusementpark.business.security;

/**
 * Protects passwords so that only a one-way protected value is stored.
 */
public interface PasswordHasher {

    /** Returns a new protected value for {@code plainPassword}. */
    String hash(String plainPassword);

    /** Whether {@code plainPassword} corresponds to the protected value {@code hash}. */
    boolean matches(String plainPassword, String hash);
}
