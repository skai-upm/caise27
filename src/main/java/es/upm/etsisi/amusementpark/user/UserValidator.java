package es.upm.etsisi.amusementpark.user;

/**
 * User data rules, implemented with plain character checks (no regular expressions).
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
        if (!isAsciiDigits(number)) {
            return false;
        }
        int expected = DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
        return dni.charAt(DNI_DIGITS) == expected;
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
        String extension = host.substring(dot + 1);
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.chars().anyMatch(Character::isUpperCase)
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

    private static boolean isAsciiDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
