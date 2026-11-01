package es.upm.amusementpark.business;

/**
 * Validation rules for user data, implemented with plain String and Character
 * operations (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String number = dni.substring(0, DNI_DIGITS);
        if (!isAsciiDigits(number)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
        return dni.charAt(DNI_DIGITS) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2) {
            return false;
        }
        String local = parts[0];
        String host = parts[1];
        int lastDot = host.lastIndexOf('.');
        if (local.isEmpty() || lastDot < 0) {
            return false;
        }
        String domain = host.substring(0, lastDot);
        String extension = host.substring(lastDot + 1);
        return !domain.isEmpty() && extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAsciiDigits(card);
    }

    private static boolean isAsciiDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean containsWhitespace(String text) {
        return containsAny(text, Character::isWhitespace);
    }

    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String text, CharPredicate predicate) {
        for (char c : text.toCharArray()) {
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
}
