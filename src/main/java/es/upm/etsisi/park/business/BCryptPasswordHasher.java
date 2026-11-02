package es.upm.etsisi.park.business;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password protection backed by the jBCrypt library.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

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

    /** Values that are not BCrypt hashes (for example legacy plain text) never match. */
    @Override
    public boolean matches(String password, String storedHash) {
        try {
            return storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
