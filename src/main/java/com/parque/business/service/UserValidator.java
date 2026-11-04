package com.parque.business.service;

/**
 * Validation rules for user data. Uses only String and Character operations (no regular expressions).
 */
public final class UserValidator {
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /** Eight digits followed by the uppercase check letter of the Spanish DNI algorithm. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String digits = dni.substring(0, 8);
        if (!allAsciiDigits(digits)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    /** Basic {@code texto@texto.xxx} check: one '@', non-empty local part and domain, 3-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int dot = host.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        String domain = host.substring(0, dot);
        String extension = host.substring(dot + 1);
        return !domain.isEmpty() && extension.length() == 3;
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
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
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

    /** Exactly 16 ASCII digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allAsciiDigits(card);
    }

    /** Non-null and containing at least one non-whitespace character. */
    public static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean allAsciiDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
