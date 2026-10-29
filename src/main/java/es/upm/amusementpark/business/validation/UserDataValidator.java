package es.upm.amusementpark.business.validation;

import es.upm.amusementpark.business.exception.InvalidValueException;

import java.util.ArrayList;
import java.util.List;

/**
 * Restrictions on user data (DNI, email, password and card number), checked with
 * basic String operations only — no regular expressions or external libraries.
 */
public final class UserDataValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserDataValidator() {
    }

    /** Eight digits followed by the letter given by the standard Spanish algorithm. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9 || !isDigits(dni.substring(0, 8))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, 8));
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
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
        String domain = parts[1];
        int dot = domain.lastIndexOf('.');
        return dot > 0 && domain.length() - dot - 1 == 3;
    }

    /** At least one upper-case letter, one lower-case letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isWhitespace(c) && !Character.isLetter(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /** Exactly 16 consecutive digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isDigits(card);
    }

    /**
     * Checks all restricted user data at once.
     *
     * @throws InvalidValueException naming every invalid field
     */
    public static void validate(String dni, String email, String password, String card) {
        List<String> invalid = new ArrayList<>();
        if (!isValidDni(dni)) {
            invalid.add("DNI");
        }
        if (!isValidEmail(email)) {
            invalid.add("email");
        }
        if (!isValidPassword(password)) {
            invalid.add("password");
        }
        if (!isValidCard(card)) {
            invalid.add("card");
        }
        if (!invalid.isEmpty()) {
            throw new InvalidValueException("invalid user data: " + String.join(", ", invalid));
        }
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return !text.isEmpty();
    }
}
