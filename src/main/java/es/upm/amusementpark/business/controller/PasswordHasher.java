package es.upm.amusementpark.business.controller;

/**
 * Protects passwords before they are stored and checks supplied passwords against
 * the stored value.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String stored);
}
