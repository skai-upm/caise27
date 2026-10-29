package es.upm.etsisi.amusementpark;

/**
 * Validation rules for user data. Uses plain String operations only (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int EMAIL_EXTENSION_LENGTH = 3;
    private static final int CARD_DIGITS = 16;

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String number = dni.substring(0, DNI_DIGITS);
        if (!allDigits(number)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
        return dni.charAt(DNI_DIGITS) == expected;
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
        String extension = host.substring(dot + 1);
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

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

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && allDigits(card);
    }

    private static boolean allDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
