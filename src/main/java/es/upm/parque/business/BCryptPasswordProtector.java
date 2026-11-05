package es.upm.parque.business;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password protection with BCrypt through the jBCrypt library.
 */
public class BCryptPasswordProtector implements PasswordProtector {

    /** BCrypt cost (log2 of the number of rounds) used in normal execution. */
    public static final int DEFAULT_COST = 10;

    private final int cost;

    public BCryptPasswordProtector() {
        this(DEFAULT_COST);
    }

    public BCryptPasswordProtector(int cost) {
        this.cost = cost;
    }

    @Override
    public String protect(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(cost));
    }

    @Override
    public boolean verify(String plainPassword, String protectedValue) {
        if (plainPassword == null || protectedValue == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, protectedValue);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            return false;
        }
    }
}
