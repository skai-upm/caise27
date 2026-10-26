package es.upm.parque.business.validation;

/**
 * Validation rules for user data. Implemented with plain String operations (no regular expressions).
 */
public final class UserValidator {
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
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
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.contains(" ")) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int dot = domain.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = domain.substring(dot + 1);
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

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
