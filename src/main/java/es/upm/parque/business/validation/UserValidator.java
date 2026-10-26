package es.upm.parque.business.validation;

import java.util.function.IntPredicate;

/**
 * Validation rules for user data. Implemented with plain String operations (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String digits = dni.substring(0, 8);
        if (!isAllDigits(digits)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(digits) % 23);
        return dni.charAt(8) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int dot = domain.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = domain.substring(dot + 1);
        return extension.length() == 3;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && isAllDigits(card);
    }

    private static boolean isAllDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return !text.isEmpty();
    }

    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String text, IntPredicate condition) {
        return text.chars().anyMatch(condition);
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
