package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt protection of passwords using jBCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private final int logRounds;

    public BCryptPasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean verify(String plain, String stored) {
        try {
            return stored != null && BCrypt.checkpw(plain, stored);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
