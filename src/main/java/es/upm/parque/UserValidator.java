package es.upm.parque;

/**
 * Validation rules for user data, written with plain String and Character operations (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * A Spanish DNI: eight digits and an uppercase check letter that matches the number.
     */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String digits = dni.substring(0, DNI_DIGITS);
        if (!allDigits(digits)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /**
     * An email of the form text@domain.xxx, where the extension has exactly three letters.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
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
        return !domain.isEmpty() && extension.length() == EMAIL_EXTENSION_LENGTH && allLetters(extension);
    }

    /**
     * A password with at least one uppercase letter, one lowercase letter, one digit and one symbol.
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
            } else if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /**
     * A card number made of exactly sixteen consecutive digits.
     */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && allDigits(card);
    }

    private static boolean allDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private static boolean allLetters(String value) {
        for (char c : value.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private static boolean containsWhitespace(String value) {
        for (char c : value.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
