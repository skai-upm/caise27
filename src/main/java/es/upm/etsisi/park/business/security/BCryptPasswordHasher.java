package es.upm.etsisi.park.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} based on jBCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    /** @param logRounds BCrypt cost factor (4..31) */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String candidate, String storedHash) {
        if (candidate == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidate, storedHash);
        } catch (IllegalArgumentException e) {
            // stored value is not a BCrypt hash (e.g. plain-text data from older versions)
            return false;
        }
    }
}
