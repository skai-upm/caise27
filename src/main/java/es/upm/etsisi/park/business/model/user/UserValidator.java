package es.upm.etsisi.park.business.model.user;

/**
 * Validation rules for user data. Implemented with plain character and
 * String operations; no regular expressions are used.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Eight digits followed by the matching Spanish control letter (case-insensitive). */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        for (int i = 0; i < DNI_DIGITS; i++) {
            if (!isAsciiDigit(dni.charAt(i))) {
                return false;
            }
        }
        int number = Integer.parseInt(dni.substring(0, DNI_DIGITS));
        char expected = DNI_LETTERS.charAt(number % DNI_LETTERS.length());
        return Character.toUpperCase(dni.charAt(DNI_DIGITS)) == expected;
    }

    /** Format text@text.xxx: one '@', non-empty parts, extension of exactly three letters. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || containsWhitespace(email)) {
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
        if (extension.length() != EMAIL_EXTENSION_LENGTH) {
            return false;
        }
        for (int i = 0; i < extension.length(); i++) {
            if (!Character.isLetter(extension.charAt(i))) {
                return false;
            }
        }
        return !hasEmptyDotSegment(parts[0]) && !hasEmptyDotSegment(domain);
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
            } else if (!Character.isWhitespace(c) && !Character.isLetter(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /** Exactly sixteen consecutive digits. */
    public static boolean isValidCard(String card) {
        if (card == null || card.length() != CARD_DIGITS) {
            return false;
        }
        for (int i = 0; i < card.length(); i++) {
            if (!isAsciiDigit(card.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEmptyDotSegment(String value) {
        return value.startsWith(".") || value.endsWith(".") || value.contains("..");
    }
}
