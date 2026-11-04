package es.upm.park.business;

/**
 * Validation rules for user data. Uses plain string and character operations only
 * (no regular expressions).
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    /** Eight digits followed by the matching Spanish DNI check letter (uppercase). */
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

    /** Basic {@code texto@texto.xxx} shape with a three-character extension. */
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
        return extension.length() == EMAIL_EXTENSION_LENGTH;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean upper = password.chars().anyMatch(Character::isUpperCase);
        boolean lower = password.chars().anyMatch(Character::isLowerCase);
        boolean digit = password.chars().anyMatch(Character::isDigit);
        boolean symbol = password.chars().anyMatch(UserValidator::isSymbol);
        return upper && lower && digit && symbol;
    }

    /** A symbol is any visible character that is neither a letter nor a digit. */
    private static boolean isSymbol(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    /** Exactly sixteen consecutive ASCII digits. */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == CARD_DIGITS && isAsciiDigits(card);
    }

    /** Required free-text values (names, surnames, phone) must not be blank. */
    public static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isAsciiDigits(String s) {
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
