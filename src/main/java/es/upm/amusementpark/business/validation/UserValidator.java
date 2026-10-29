package es.upm.amusementpark.business.validation;

/**
 * Validation rules for user data. Uses plain String/char operations only (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int EMAIL_EXTENSION_LENGTH = 3;
    private static final int CARD_DIGITS = 16;

    private UserValidator() {
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
        if (email == null || email.indexOf('@') != email.lastIndexOf('@')) {
            return false;
        }
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isEmpty() || email.endsWith("@")) {
            return false;
        }
        String domainPart = parts[1];
        int dot = domainPart.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = domainPart.substring(dot + 1);
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAsciiDigits(card);
    }

    private static boolean isAsciiDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return !value.isEmpty();
    }
}
