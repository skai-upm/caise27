package es.upm.amusementpark.business.security;

import org.mindrot.jbcrypt.BCrypt;

/** Protects passwords with BCrypt and verifies entered passwords against stored values. */
public class PasswordProtector {

    /** @return a new BCrypt-protected value for the password (a fresh salt each time) */
    public String protect(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /** @return whether the entered password matches the stored protected value */
    public boolean matches(String plainPassword, String protectedValue) {
        if (plainPassword == null || protectedValue == null || protectedValue.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, protectedValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
