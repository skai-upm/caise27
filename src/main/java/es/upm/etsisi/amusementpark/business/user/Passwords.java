package es.upm.etsisi.amusementpark.business.user;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt protection of user passwords. Only hashes are ever stored.
 */
public final class Passwords {
    private static final String BCRYPT_PREFIX = "$2";

    private Passwords() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * True if the plain password matches the stored hash. Values that are not BCrypt
     * hashes (legacy plaintext files, reconstructed former users) never match.
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !storedHash.startsWith(BCRYPT_PREFIX)) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
