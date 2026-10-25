package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password encoder based on the jBCrypt library.
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordEncoder() {
        this(DEFAULT_LOG_ROUNDS);
    }

    public BCryptPasswordEncoder(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plaintext, String hash) {
        if (plaintext == null || !isHash(hash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(plaintext, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean isHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
