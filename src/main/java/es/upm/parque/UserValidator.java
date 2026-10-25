package es.upm.parque;

/**
 * Validation rules for user data, implemented with String and Character operations only.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int CARD_LENGTH = 16;

    private UserValidator() {
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        String digits = dni.substring(0, 8);
        if (!allDigits(digits)) {
            return false;
        }
        char letter = Character.toUpperCase(dni.charAt(8));
        if (!Character.isLetter(letter)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return DNI_LETTERS.charAt(number % 23) == letter;
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
        int lastDot = host.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String domain = host.substring(0, lastDot);
        String extension = host.substring(lastDot + 1);
        return !domain.isEmpty() && extension.length() == 3;
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
        return card != null && card.length() == CARD_LENGTH && allDigits(card);
    }

    private static boolean allDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
