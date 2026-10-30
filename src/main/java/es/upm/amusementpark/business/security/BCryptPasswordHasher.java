package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} based on BCrypt. Stored values that are not BCrypt hashes (plaintext passwords written
 * by earlier deliveries) are compared literally; they are not migrated automatically.
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
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null) {
            return false;
        }
        if (isBCryptHash(storedValue)) {
            return BCrypt.checkpw(rawPassword, storedValue);
        }
        return rawPassword.equals(storedValue);
    }

    public static boolean isBCryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
