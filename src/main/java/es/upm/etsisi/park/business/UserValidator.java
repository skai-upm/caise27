package es.upm.etsisi.park.business;

/**
 * User data validation rules. Implemented with plain character and String
 * operations only: no regular expressions or external libraries.
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
        if (!isAsciiDigits(digits)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
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
        return password != null
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(UserValidator::isSymbol);
    }

    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAsciiDigits(card);
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

    private static boolean containsWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
