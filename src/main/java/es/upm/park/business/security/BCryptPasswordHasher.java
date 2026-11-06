package es.upm.park.business.security;

import org.mindrot.jbcrypt.BCrypt;

/** {@link PasswordHasher} based on the jBCrypt library. */
public class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
