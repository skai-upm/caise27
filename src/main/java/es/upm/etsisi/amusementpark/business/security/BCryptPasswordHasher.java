package es.upm.etsisi.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/** {@link PasswordHasher} backed by the jBCrypt library. */
public class BCryptPasswordHasher implements PasswordHasher {

    /** Work factor used by the application (jBCrypt's default). */
    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            // jBCrypt rejects values that are not BCrypt hashes (e.g. legacy plaintext or empty)
            return false;
        }
    }
}
