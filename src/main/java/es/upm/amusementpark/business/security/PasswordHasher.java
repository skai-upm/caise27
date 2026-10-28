package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Protects passwords with BCrypt; only the protected value is ever stored.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Tells whether the password corresponds to the stored BCrypt value. Values that are not
     * BCrypt hashes never match.
     */
    public static boolean matches(String password, String storedHash) {
        try {
            return storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
