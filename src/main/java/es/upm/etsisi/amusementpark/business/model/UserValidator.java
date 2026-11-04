package es.upm.etsisi.amusementpark.business.model;

/**
 * Validation rules for user data. Uses plain string operations only
 * (no regular expressions, no external libraries).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    /** Eight digits followed by the matching uppercase Spanish check letter. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String digits = dni.substring(0, DNI_DIGITS);
        if (!isAsciiDigits(digits)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /** Basic {@code text@text.xxx} check with a three-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return false;
        }
        String extension = domain.substring(lastDot + 1);
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        return password != null
                && containsChar(password, Character::isUpperCase)
                && containsChar(password, Character::isLowerCase)
                && containsChar(password, Character::isDigit)
                && containsChar(password, UserValidator::isSymbol);
    }

    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsChar(String value, CharPredicate predicate) {
        for (char c : value.toCharArray()) {
            if (predicate.test(c)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char c);
    }

    /** Exactly sixteen ASCII digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAsciiDigits(card);
    }

    /** Non-null and containing at least one non-whitespace character. */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isAsciiDigits(String value) {
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
