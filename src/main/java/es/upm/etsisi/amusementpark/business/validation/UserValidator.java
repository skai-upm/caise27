package es.upm.etsisi.amusementpark.business.validation;

/**
 * Validation rules for user data. Uses plain String operations only (no regular expressions).
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
        if (!isAllDigits(number)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
        return dni.charAt(DNI_DIGITS) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        return parts.length == 2 && !parts[0].isEmpty() && hasValidDomain(parts[1]);
    }

    private static boolean hasValidDomain(String domain) {
        int lastDot = domain.lastIndexOf('.');
        return lastDot > 0 && domain.length() - lastDot - 1 == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(UserValidator::isSymbol);
    }

    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    public static boolean isValidCardNumber(String card) {
        return card != null && card.length() == CARD_DIGITS && isAllDigits(card);
    }

    private static boolean isAllDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
