package es.upm.amusementpark.business;

import org.mindrot.jbcrypt.BCrypt;

/**
 * {@link PasswordHasher} based on BCrypt (jBCrypt), with a new random salt for every protection.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public String protect(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password is required");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    @Override
    public boolean verify(String plainPassword, String storedValue) {
        if (plainPassword == null || storedValue == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedValue);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            return false;
        }
    }
}
