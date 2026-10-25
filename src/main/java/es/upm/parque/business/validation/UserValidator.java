package es.upm.parque.business.validation;

/**
 * Validation rules for user data, implemented with plain String operations (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /**
     * A DNI is eight digits followed by the check letter for that number (number mod 23).
     */
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
        return DNI_LETTERS.charAt(number % 23) == letter;
    }

    /**
     * An email has the form texto@texto.xxx with a three-character extension.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.contains(" ")) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int lastDot = host.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String domain = host.substring(0, lastDot);
        String extension = host.substring(lastDot + 1);
        return !domain.isEmpty() && extension.length() == 3;
    }

    /**
     * A password needs an uppercase letter, a lowercase letter, a digit and a symbol.
     */
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
            } else if (!Character.isWhitespace(c) && !Character.isLetter(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /**
     * A card number is exactly 16 consecutive digits.
     */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isAllDigits(card);
    }

    private static boolean isAllDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
