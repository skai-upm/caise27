package es.upm.etsisi.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt protection of passwords using the jBCrypt library.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String plainPassword, String hash) {
        try {
            return BCrypt.checkpw(plainPassword, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
