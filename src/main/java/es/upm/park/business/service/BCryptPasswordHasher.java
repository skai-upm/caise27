package es.upm.park.business.service;

import org.mindrot.jbcrypt.BCrypt;

/** {@link PasswordHasher} backed by the jBCrypt library. */
public class BCryptPasswordHasher implements PasswordHasher {

    /** Default BCrypt cost factor (2^10 rounds). */
    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plaintext, String storedHash) {
        if (plaintext == null || storedHash == null || !isBCryptHash(storedHash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(plaintext, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isBCryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
