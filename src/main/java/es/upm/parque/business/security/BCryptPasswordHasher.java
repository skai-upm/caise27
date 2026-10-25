package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing with the BCrypt algorithm of the jBCrypt library.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int HASH_LENGTH = 60;

    private final int logRounds;

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String plainPassword, String storedHash) {
        return isHash(storedHash) && BCrypt.checkpw(plainPassword, storedHash);
    }

    @Override
    public boolean isHash(String storedValue) {
        return storedValue != null && storedValue.length() == HASH_LENGTH
                && (storedValue.startsWith("$2a$") || storedValue.startsWith("$2b$")
                || storedValue.startsWith("$2y$"));
    }
}
