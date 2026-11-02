package com.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password protection. Stored values written before passwords were hashed (no BCrypt
 * prefix) are compared literally and are not migrated automatically.
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
        if (password == null || stored == null) {
            return false;
        }
        if (isBCryptHash(stored)) {
            return BCrypt.checkpw(password, stored);
        }
        return !stored.isEmpty() && stored.equals(password);
    }

    public static boolean isBCryptHash(String stored) {
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }
}
