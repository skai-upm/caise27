package es.upm.park.business.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt protection using the jBCrypt library. The cost (log rounds) comes
 * from the {@code park.bcrypt.rounds} system property, 10 by default.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    static final String ROUNDS_PROPERTY = "park.bcrypt.rounds";
    private static final int DEFAULT_ROUNDS = 10;

    private final int rounds;

    public BCryptPasswordHasher() {
        this(Integer.getInteger(ROUNDS_PROPERTY, DEFAULT_ROUNDS));
    }

    public BCryptPasswordHasher(int rounds) {
        this.rounds = rounds;
    }

    @Override
    public String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(rounds));
    }

    /** A stored value that is not a BCrypt hash never matches. */
    @Override
    public boolean matches(String plain, String stored) {
        if (plain == null || stored == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, stored);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
