package es.upm.amusementpark.business.validation;

/**
 * Validation rules for user data. Uses plain String operations only (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String number = dni.substring(0, 8);
        if (!allDigits(number)) {
            return false;
        }
        char letter = dni.charAt(8);
        int index = Integer.parseInt(number) % 23;
        return letter == DNI_LETTERS.charAt(index);
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int dot = host.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String domain = host.substring(0, dot);
        String extension = host.substring(dot + 1);
        if (extension.length() != 3 || extension.indexOf(' ') >= 0) {
            return false;
        }
        return !domain.startsWith(".") && !domain.endsWith(".") && !domain.contains("..");
    }

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

    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allDigits(card);
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
