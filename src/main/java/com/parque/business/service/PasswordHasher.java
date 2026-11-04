package com.parque.business.service;

/**
 * Protects passwords: produces a one-way hash for storage and checks a supplied password against it.
 */
public interface PasswordHasher {
    String hash(String plainPassword);

    /** True if {@code plainPassword} corresponds to {@code storedHash}; false for anything that is not a valid hash. */
    boolean matches(String plainPassword, String storedHash);
}
