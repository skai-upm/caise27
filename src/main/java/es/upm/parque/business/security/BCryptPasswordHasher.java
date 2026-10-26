package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing with BCrypt (external library org.mindrot:jbcrypt).
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int HASH_LENGTH = 60;

    private final int cost;

    /**
     * @param cost BCrypt work factor (log2 of the number of rounds), between 4 and 31
     */
    public BCryptPasswordHasher(int cost) {
        this.cost = cost;
    }

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(cost));
    }

    @Override
    public boolean matches(String plainPassword, String hash) {
        return isHash(hash) && BCrypt.checkpw(plainPassword, hash);
    }

    @Override
    public boolean isHash(String value) {
        return value != null && value.length() == HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
