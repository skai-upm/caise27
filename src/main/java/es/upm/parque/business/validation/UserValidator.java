package es.upm.parque.business.validation;

/**
 * Validation rules for user data, implemented with plain String operations (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        for (int i = 0; i < 8; i++) {
            if (!isAsciiDigit(dni.charAt(i))) {
                return false;
            }
        }
        int number = Integer.parseInt(dni.substring(0, 8));
        char expected = DNI_LETTERS.charAt(number % 23);
        return Character.toUpperCase(dni.charAt(8)) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
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
        String extension = domain.substring(lastDot + 1);
        return extension.length() == 3;
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
        if (card == null || card.length() != 16) {
            return false;
        }
        for (char c : card.toCharArray()) {
            if (!isAsciiDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
