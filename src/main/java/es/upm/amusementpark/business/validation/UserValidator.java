package es.upm.amusementpark.business.validation;

import java.util.function.IntPredicate;

/**
 * Validation rules for user data. Implemented with basic String and Character
 * operations only (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;
    private static final int BCRYPT_HASH_LENGTH = 60;

    private UserValidator() {
    }

    public static boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Eight digits followed by the Spanish check letter (case-insensitive). */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String number = dni.substring(0, DNI_DIGITS);
        if (!isAllDigits(number)) {
            return false;
        }
        char letter = Character.toUpperCase(dni.charAt(DNI_DIGITS));
        return letter == DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
    }

    /** Basic {@code text@text.xxx} check with an extension of exactly three characters. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || containsWhitespace(email)) {
            return false;
        }
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        String domain = email.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        if (domain.length() - dot - 1 != EMAIL_EXTENSION_LENGTH) {
            return false;
        }
        return domain.charAt(0) != '.' && !domain.contains("..");
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    /** Whether {@code value} has the shape of a BCrypt hash ({@code $2a$}, {@code $2b$} or {@code $2y$}, 60 characters). */
    public static boolean isPasswordHash(String value) {
        return value != null && value.length() == BCRYPT_HASH_LENGTH
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    /** Exactly sixteen consecutive digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAllDigits(card);
    }

    private static boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
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
        for (int i = 0; i < value.length(); i++) {
            if (predicate.test(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
