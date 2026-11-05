package es.upm.amusementpark.business;

/**
 * Protects passwords before they are stored and verifies entered passwords against stored values.
 */
public interface PasswordHasher {

    /**
     * @param plainPassword a password that already passed validation
     * @return the protected value to store instead of the password
     */
    String protect(String plainPassword);

    /**
     * @param plainPassword the entered password
     * @param storedValue the stored protected value
     * @return whether the password matches; {@code false} for values that are not valid protected values
     */
    boolean verify(String plainPassword, String storedValue);
}
