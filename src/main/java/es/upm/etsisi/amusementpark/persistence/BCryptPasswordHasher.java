package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.PasswordHasher;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt protection of stored passwords, using the jBCrypt library.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    /** @param logRounds BCrypt cost factor (4 to 31) */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    /** Values that are not BCrypt hashes (e.g. legacy plaintext) never match. */
    @Override
    public boolean matches(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
