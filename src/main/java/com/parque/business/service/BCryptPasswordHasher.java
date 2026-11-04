package com.parque.business.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} backed by the BCrypt library (org.mindrot:jbcrypt).
 */
public class BCryptPasswordHasher implements PasswordHasher {
    /** Library default work factor. */
    public static final int DEFAULT_LOG_ROUNDS = 10;

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
        if (storedHash == null || !storedHash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException notAHash) {
            return false;
        }
    }
}
