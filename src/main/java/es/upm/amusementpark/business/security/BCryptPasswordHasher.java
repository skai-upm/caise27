package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password protection using jBCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    /**
     * jBCrypt's default work factor.
     */
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

    /**
     * A stored value that is not a BCrypt hash (e.g. a plain password from an older data file) never matches.
     */
    @Override
    public boolean matches(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            return false;
        }
    }
}
