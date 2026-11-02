package com.amusementpark.business.security;

/**
 * Protects passwords before they are stored and verifies supplied passwords against them.
 */
public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String stored);
}
