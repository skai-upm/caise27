package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.security.PasswordPolicy;

/**
 * Restrictions on the data that defines a user (DNI, e-mail, password and card),
 * checked with basic String operations; the telephone number is only stored.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /** Eight digits followed by the letter given by the Spanish DNI algorithm. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9 || !isDigits(dni.substring(0, 8))) {
            return false;
        }
        return dni.charAt(8) == DNI_LETTERS.charAt(Integer.parseInt(dni.substring(0, 8)) % DNI_LETTERS.length());
    }

    /** {@code text@text.xxx}: content before a single @, a domain and a three-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        int dot = parts[1].lastIndexOf('.');
        return dot > 0 && parts[1].length() - dot - 1 == 3;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        try {
            PasswordPolicy.validate(password);
            return true;
        } catch (InvalidValueException e) {
            return false;
        }
    }

    /** Exactly 16 consecutive digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isDigits(card);
    }

    /**
     * Checks the user data that has restrictions.
     *
     * @throws InvalidValueException naming the first invalid field
     */
    public static void validate(String dni, String email, String password, String card) {
        if (!isValidDni(dni)) {
            throw new InvalidValueException("Invalid DNI: " + dni);
        }
        if (!isValidEmail(email)) {
            throw new InvalidValueException("Invalid email: " + email);
        }
        PasswordPolicy.validate(password);
        if (!isValidCard(card)) {
            throw new InvalidValueException("Invalid card number: " + card);
        }
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
