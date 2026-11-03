package es.upm.etsisi.amusementpark.business;

/**
 * Validation rules for user data, shared by user creation and update.
 * Implemented with plain String operations: no regular expressions, no external libraries.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    /** Eight digits followed by the uppercase letter given by the Spanish DNI algorithm. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String digits = dni.substring(0, DNI_DIGITS);
        if (!isAllAsciiDigits(digits)) {
            return false;
        }
        int number = Integer.parseInt(digits);
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /** Basic {@code text@text.xxx} check: non-empty local part, non-empty domain, 3-char extension. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        boolean valid = parts.length == 2 && !parts[0].isEmpty();
        if (valid) {
            String host = parts[1];
            int dot = host.lastIndexOf('.');
            // dot > 0: a non-empty domain precedes the last period; the rest is the extension
            valid = dot > 0 && host.length() - dot - 1 == EMAIL_EXTENSION_LENGTH;
        }
        return valid;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
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

    /** Exactly 16 consecutive digits; no spaces, letters or symbols. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAllAsciiDigits(card);
    }

    /** Required free-text fields (name, surname, phone) must not be blank. */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isAllAsciiDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
