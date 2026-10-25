package es.upm.parque;

/**
 * Validation rules for user data. Uses only String operations and split (no regular expressions).
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
        if (!allDigits(digits)) {
            return false;
        }
        char letter = Character.toUpperCase(dni.charAt(8));
        int number = Integer.parseInt(digits);
        return DNI_LETTERS.charAt(number % 23) == letter;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int lastDot = domain.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String domainName = domain.substring(0, lastDot);
        String extension = domain.substring(lastDot + 1);
        return !domainName.isEmpty() && extension.length() == 3;
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
        return card != null && card.length() == 16 && allDigits(card);
    }

    public static boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
    }

    private static boolean allDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
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
