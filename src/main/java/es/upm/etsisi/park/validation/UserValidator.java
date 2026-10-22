package es.upm.etsisi.park.validation;

/**
 * Validation rules for user data. Implemented with plain String/Character operations
 * (no regular expressions, no external libraries).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

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
        char letter = Character.toUpperCase(dni.charAt(DNI_DIGITS));
        return DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length()) == letter;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int dot = domain.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = domain.substring(dot + 1);
        return extension.length() == EMAIL_EXTENSION_LENGTH && allLetters(extension);
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && allDigits(card);
    }

    public static boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
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

    private static boolean allDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean allLetters(String text) {
        for (char c : text.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsWhitespace(String text) {
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
