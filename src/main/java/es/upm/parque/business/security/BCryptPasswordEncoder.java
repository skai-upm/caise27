package es.upm.parque.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password encoder based on the BCrypt library declared in pom.xml.
 */
public class BCryptPasswordEncoder implements PasswordEncoder {
    public static final int DEFAULT_COST = 10;

    private final int cost;

    public BCryptPasswordEncoder() {
        this(DEFAULT_COST);
    }

    public BCryptPasswordEncoder(int cost) {
        this.cost = cost;
    }

    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(cost));
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        if (!isHash(hash)) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hash);
    }

    @Override
    public boolean isHash(String value) {
        return value != null && value.length() == 60
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
