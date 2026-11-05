package es.upm.parque.business;

/**
 * Replaceable behaviour that protects user passwords before they are stored and verifies entered passwords
 * against the stored protected value.
 */
public interface PasswordProtector {

    /** Returns a protected value of the plain password; the plain password cannot be recovered from it. */
    String protect(String plainPassword);

    /** Whether the plain password corresponds to the protected value; malformed values never match. */
    boolean verify(String plainPassword, String protectedValue);
}
