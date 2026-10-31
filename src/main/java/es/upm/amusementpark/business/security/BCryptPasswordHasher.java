package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password protection. Stored values that are not BCrypt hashes were
 * written by earlier deliveries in plaintext; they are still verified by
 * direct comparison and are not migrated automatically.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    public static final int DEFAULT_COST = 10;

    private static final String BCRYPT_PREFIX = "$2";

    private final int cost;

    public BCryptPasswordHasher() {
        this(DEFAULT_COST);
    }

    public BCryptPasswordHasher(int cost) {
        this.cost = cost;
    }

    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(cost));
    }

    @Override
    public boolean matches(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null) {
            return false;
        }
        if (!isHash(storedValue)) {
            return storedValue.equals(rawPassword);
        }
        try {
            return BCrypt.checkpw(rawPassword, storedValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Whether the stored value is a BCrypt hash rather than legacy plaintext. */
    public static boolean isHash(String storedValue) {
        return storedValue.startsWith(BCRYPT_PREFIX);
    }
}
