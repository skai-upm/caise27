package es.upm.amusementpark.business.security;

/**
 * Protects passwords and verifies entered passwords against protected values.
 */
public interface PasswordEncoder {

    String encode(String plainPassword);

    boolean matches(String plainPassword, String protectedValue);
}
