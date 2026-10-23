package es.upm.amusementpark.business.security;

import es.upm.amusementpark.business.validation.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} based on the jBCrypt library.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    /** Work factor used by the application. */
    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    /**
     * @param logRounds BCrypt work factor (4 to 31); every extra round doubles the hashing time
     */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plainPassword, String storedHash) {
        // the empty value kept for deleted users, or anything else that is not a hash, never matches
        if (plainPassword == null || !UserValidator.isPasswordHash(storedHash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
