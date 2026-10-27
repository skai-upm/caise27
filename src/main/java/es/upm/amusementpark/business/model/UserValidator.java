package es.upm.amusementpark.business.model;

/**
 * Validity rules for user data. Implemented with plain character and String
 * operations (no regular expressions), so they can be reused by every user operation.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    public static boolean isValid(User user) {
        return user != null
                && isValidDni(user.getDni())
                && isValidEmail(user.getEmail())
                && isValidPassword(user.getPassword())
                && isValidCard(user.getCardNumber());
    }

    /** Eight digits followed by the matching uppercase check letter. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9 || !allDigits(dni.substring(0, 8))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, 8));
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    /** Format text@text.xxx: one '@', non-empty local part, domain and a three-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        String host = email.substring(at + 1);
        int dot = host.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = host.substring(dot + 1);
        return extension.length() == 3;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
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

    /** Exactly sixteen consecutive digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allDigits(card);
    }

    private static boolean allDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
