package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt implementation of {@link PasswordHasher}.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;
    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    /**
     * @param logRounds BCrypt work factor (4..31)
     */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plaintext, String hash) {
        return isHash(hash) && BCrypt.checkpw(plaintext, hash);
    }

    @Override
    public boolean isHash(String stored) {
        return stored != null && stored.length() == HASH_LENGTH && stored.startsWith("$2a$");
    }
}
