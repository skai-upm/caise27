package es.upm.etsisi.amusementpark.business;

/**
 * Validation rules for user data, implemented with plain String and Character
 * operations (no regular expressions, no external libraries).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1 || !allDigits(dni.substring(0, DNI_DIGITS))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, DNI_DIGITS));
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /** Accepts text@text.xxx: non-empty local part, non-empty domain, 3-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int lastDot = host.lastIndexOf('.');
        if (lastDot <= 0) {
            return false;
        }
        String domain = host.substring(0, lastDot);
        String extension = host.substring(lastDot + 1);
        if (extension.length() != EMAIL_EXTENSION_LENGTH) {
            return false;
        }
        return !domain.startsWith(".") && !domain.endsWith(".") && !domain.contains("..");
    }

    /** Requires at least one uppercase letter, lowercase letter, digit and symbol. */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (char c : password.toCharArray()) {
            upper |= Character.isUpperCase(c);
            lower |= Character.isLowerCase(c);
            digit |= Character.isDigit(c);
            symbol |= isSymbol(c);
        }
        return upper && lower && digit && symbol;
    }

    /** A symbol is any character that is neither a letter, a digit nor whitespace. */
    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && allDigits(card);
    }

    private static boolean allDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
