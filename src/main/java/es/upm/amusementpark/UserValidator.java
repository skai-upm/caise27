package es.upm.amusementpark;

/**
 * Validation rules for user data. Implemented with basic String operations only (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /**
     * Eight digits followed by the uppercase check letter given by the Spanish DNI algorithm.
     */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9 || !allDigits(dni.substring(0, 8))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, 8));
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    /**
     * Format text@text.xxx: one '@', content before it, a domain and an extension of exactly three characters.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.indexOf('@') != email.lastIndexOf('@')) {
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
        return extension.length() == 3;
    }

    /**
     * At least one uppercase letter, one lowercase letter, one digit and one symbol.
     */
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

    /**
     * Exactly 16 consecutive digits.
     */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allDigits(card);
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
