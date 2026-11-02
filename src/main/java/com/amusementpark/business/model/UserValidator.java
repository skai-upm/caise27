package com.amusementpark.business.model;

/**
 * Validation rules for user data, implemented with plain String and Character operations.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Eight digits followed by the letter given by the Spanish DNI algorithm. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String digits = dni.substring(0, 8);
        if (!isAllDigits(digits)) {
            return false;
        }
        char letter = Character.toUpperCase(dni.charAt(8));
        int number = Integer.parseInt(digits);
        return letter == DNI_LETTERS.charAt(number % 23);
    }

    /** Format texto@texto.xxx with an extension of exactly three characters. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return false;
        }
        String extension = domain.substring(lastDot + 1);
        return extension.length() == 3;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
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

    /** Exactly sixteen consecutive digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isAllDigits(card);
    }

    private static boolean isAllDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
