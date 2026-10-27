package es.upm.amusementpark.business.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Protects passwords with BCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /** Stored values that are not BCrypt hashes (for example old plain text) never match. */
    @Override
    public boolean matches(String password, String storedValue) {
        try {
            return BCrypt.checkpw(password, storedValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
