package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes and verifies passwords with BCrypt.
 */
public class PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public PasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

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
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * True if the value looks like a BCrypt hash ($2a$, $2b$ or $2y$ prefix, 60 characters).
     */
    public static boolean isHash(String value) {
        return value != null && value.length() == 60
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
