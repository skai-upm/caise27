package com.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes and verifies passwords with BCrypt.
 */
public class PasswordHasher {
    public static final int DEFAULT_LOG_ROUNDS = 10;
    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public PasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    public boolean matches(String password, String hash) {
        if (password == null || !isHash(hash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Whether the stored value looks like a BCrypt hash rather than a legacy plain-text password.
     */
    public boolean isHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
