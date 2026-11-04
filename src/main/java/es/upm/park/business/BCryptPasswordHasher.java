package es.upm.park.business;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt implementation of {@link PasswordHasher} using the jBCrypt library; every hash gets a
 * new random salt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int DEFAULT_LOG_ROUNDS = 10;
    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || !looksLikeBcrypt(storedHash)) {
            // stored value is not a BCrypt hash (e.g. a record written before hashing)
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean looksLikeBcrypt(String value) {
        return value != null && value.length() == HASH_LENGTH && value.startsWith("$2");
    }
}
