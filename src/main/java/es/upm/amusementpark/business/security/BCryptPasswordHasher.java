package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt-based {@link PasswordHasher}. Stored values that are not BCrypt hashes
 * were written by earlier deliveries in plaintext; they are still verified by
 * exact comparison so existing users can authenticate, and are only replaced
 * by a hash when the password is changed.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;
    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String password, String stored) {
        if (!isHash(stored)) {
            return stored.equals(password);
        }
        try {
            return BCrypt.checkpw(password, stored);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Whether a stored value is a BCrypt hash rather than a legacy plaintext password. */
    public static boolean isHash(String stored) {
        return stored.length() == HASH_LENGTH
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }
}
