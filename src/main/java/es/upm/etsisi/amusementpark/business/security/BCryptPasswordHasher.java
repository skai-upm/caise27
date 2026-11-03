package es.upm.etsisi.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/** {@link PasswordHasher} based on BCrypt, with a fresh random salt for every hash. */
public class BCryptPasswordHasher implements PasswordHasher {

    /** Library default work factor. */
    public static final int DEFAULT_LOG_ROUNDS = 10;
    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    /** @param logRounds BCrypt work factor (4 to 31) */
    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(logRounds));
    }

    /** A stored value that is not a BCrypt hash (e.g. a legacy plaintext password) never matches. */
    @Override
    public boolean matches(String plain, String storedHash) {
        return plain != null && isBCryptHash(storedHash) && BCrypt.checkpw(plain, storedHash);
    }

    private static boolean isBCryptHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
