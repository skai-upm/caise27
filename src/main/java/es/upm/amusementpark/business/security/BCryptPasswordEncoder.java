package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password protection using the jBCrypt library.
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int LOG_ROUNDS = 10;

    @Override
    public String encode(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean matches(String plainPassword, String protectedValue) {
        if (plainPassword == null || protectedValue == null || !protectedValue.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, protectedValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
