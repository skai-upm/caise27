package es.upm.amusementpark;

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
        String digits = dni.substring(0, 8);
        if (!allDigits(digits)) {
            return false;
        }
        char expected = DNI_LETTERS.charAt(Integer.parseInt(digits) % 23);
        return dni.charAt(8) == expected;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String domainPart = parts[1];
        int lastDot = domainPart.lastIndexOf('.');
        if (lastDot <= 0) {
            return false;
        }
        String extension = domainPart.substring(lastDot + 1);
        return extension.length() == 3;
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
            upper |= Character.isUpperCase(c);
            lower |= Character.isLowerCase(c);
            digit |= Character.isDigit(c);
            symbol |= isSymbol(c);
        }
        return upper && lower && digit && symbol;
    }

    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allDigits(card);
    }

    private static boolean allDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
