package es.upm.etsisi.park;

/**
 * Validation rules for user data, implemented with plain String and
 * character operations (no regular expressions, no external libraries).
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
        if (lastDot <= 0 || domain.startsWith(".") || domain.contains("..")) {
            return false;
        }
        return domain.length() - lastDot - 1 == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String value, CharPredicate predicate) {
        for (int i = 0; i < value.length(); i++) {
            if (predicate.test(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char c);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && allDigits(card);
    }

    private static boolean allDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
