package es.upm.etsisi.amusementpark.business.user;

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
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    /** Values that are not BCrypt hashes (e.g. plaintext from earlier deliveries) never match. */
    @Override
    public boolean matches(String password, String storedHash) {
        try {
            return storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
