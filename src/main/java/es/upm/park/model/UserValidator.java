package es.upm.park.model;

import java.util.function.IntPredicate;

/**
 * Rules that decide whether the data defining a user is valid.
 * Implemented with plain String/char operations: no regular expressions
 * and no external libraries.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    /**
     * A DNI is eight digits followed by the uppercase check letter of the
     * standard Spanish algorithm.
     */
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

    /**
     * An email has the form text@text.xxx: content before '@', a domain after
     * it and an extension of exactly three characters.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int dot = host.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        return host.length() - dot - 1 == EMAIL_EXTENSION_LENGTH;
    }

    /**
     * A password contains at least one uppercase letter, one lowercase letter,
     * one digit and one symbol (any non-letter, non-digit, non-whitespace character).
     */
    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, Character::isDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String text, IntPredicate category) {
        return text.chars().anyMatch(category);
    }

    /**
     * A card number is exactly 16 consecutive digits.
     */
    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.length() == CARD_DIGITS && isAsciiDigits(cardNumber);
    }

    /**
     * A user is valid when its DNI, email, password and card number are valid.
     * The telephone number is stored as-is and is not validated.
     */
    public static boolean isValid(User user) {
        return user != null
                && isValidDni(user.getDni())
                && isValidEmail(user.getEmail())
                && isValidPassword(user.getPassword())
                && isValidCardNumber(user.getCardNumber());
    }

    private static boolean isAsciiDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
