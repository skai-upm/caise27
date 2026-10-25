package es.upm.etsisi.park.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} backed by jBCrypt.
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

    /**
     * @return {@code false} also when the stored value is not a BCrypt hash (e.g. legacy plain text)
     */
    @Override
    public boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
