package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks user data against the assignment restrictions. Only String
 * operations are used (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String number = dni.substring(0, 8);
        if (!isAllDigits(number)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(number) % 23);
        return dni.charAt(8) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int dot = host.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        return host.length() - dot - 1 == 3;
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isAllDigits(card);
    }

    /**
     * Validates a complete set of user data.
     *
     * @return the error messages, empty when every field is valid
     */
    public static List<String> validate(String dni, String email, String password, String card) {
        List<String> errors = new ArrayList<>();
        if (!isValidDni(dni)) {
            errors.add("Invalid DNI: it must have eight digits and the correct final letter.");
        }
        if (!isValidEmail(email)) {
            errors.add("Invalid email: expected format text@text.xxx.");
        }
        if (!isValidPassword(password)) {
            errors.add("Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol.");
        }
        if (!isValidCard(card)) {
            errors.add("Invalid card number: it must have exactly 16 digits.");
        }
        return errors;
    }

    private static boolean isAllDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
