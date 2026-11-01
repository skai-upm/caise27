package es.upm.amusementpark.persistence;

import org.mindrot.jbcrypt.BCrypt;

import es.upm.amusementpark.business.controller.PasswordHasher;

/**
 * BCrypt-based {@link PasswordHasher}. Stored values that are not BCrypt hashes are
 * legacy plaintext passwords from earlier deliveries and are compared literally;
 * they are replaced by a hash the next time the password changes.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_LOG_ROUNDS = 10;

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(DEFAULT_LOG_ROUNDS);
    }

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String password, String stored) {
        if (isBCryptHash(stored)) {
            return BCrypt.checkpw(password, stored);
        }
        return stored.equals(password);
    }

    static boolean isBCryptHash(String value) {
        return value.length() == 60
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
