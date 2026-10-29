package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} backed by jBCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    /**
     * Malformed or empty stored values (e.g. legacy plain-text passwords) never match.
     */
    @Override
    public boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
