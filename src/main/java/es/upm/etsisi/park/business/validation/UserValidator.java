package es.upm.etsisi.park.business.validation;

import java.util.function.IntPredicate;

/**
 * Validation rules for user data. Implemented with plain String operations (no regular expressions).
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
        String digits = dni.substring(0, DNI_DIGITS);
        if (!isAllDigits(digits)) {
            return false;
        }
        char letter = dni.charAt(DNI_DIGITS);
        int number = Integer.parseInt(digits);
        return letter == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
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
        if (lastDot <= 0) {
            return false;
        }
        String name = domain.substring(0, lastDot);
        String extension = domain.substring(lastDot + 1);
        if (name.startsWith(".") || name.endsWith(".") || name.contains("..")) {
            return false;
        }
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAllDigits(card);
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
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

    private static boolean containsWhitespace(String value) {
        return containsAny(value, Character::isWhitespace);
    }

    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String value, IntPredicate predicate) {
        return value.chars().anyMatch(predicate);
    }
}
