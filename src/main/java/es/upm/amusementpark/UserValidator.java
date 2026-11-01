package es.upm.amusementpark;

/**
 * Validation rules for user data, implemented with plain String operations.
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
        if (email == null || email.isEmpty() || containsWhitespace(email)) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String domain = parts[1];
        int dot = domain.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        return domain.length() - dot - 1 == 3;
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
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
        for (int i = 0; i < card.length(); i++) {
            if (!isAsciiDigit(card.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
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
