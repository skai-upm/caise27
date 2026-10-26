package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} based on BCrypt (jBCrypt library).
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    /** @param logRounds BCrypt work factor (4 to 30); higher is slower and stronger */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plainPassword, String hash) {
        return isHash(hash) && BCrypt.checkpw(plainPassword, hash);
    }

    @Override
    public boolean isHash(String value) {
        return value != null && value.length() == HASH_LENGTH
            && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
