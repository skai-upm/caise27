package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes and verifies passwords with BCrypt.
 */
public class PasswordHasher {

    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    /**
     * @param logRounds BCrypt cost factor (4 to 31); each increment doubles the hashing time
     */
    public PasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    public boolean matches(String password, String hash) {
        if (!isHash(hash)) {
            return false;
        }
        return BCrypt.checkpw(password, hash);
    }

    /**
     * True if the value has the shape of a BCrypt hash.
     */
    public boolean isHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
