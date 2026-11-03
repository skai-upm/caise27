package es.upm.etsisi.park.business;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Protects passwords with BCrypt. Only hashes are ever stored.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Checks a password against a stored hash. Values that are not BCrypt hashes
     * (for example plaintext files from earlier deliveries) never match.
     */
    public static boolean matches(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
