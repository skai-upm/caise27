package es.upm.parque.business.model;

/**
 * Validation rules for user data (DNI, email, password and card number).
 *
 * <p>Stateless and reusable by any user operation. No regular expressions are used.
 * The telephone number is intentionally not validated in this delivery.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /** Eight digits followed by the matching uppercase Spanish check letter. */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }
        int number = 0;
        for (int i = 0; i < 8; i++) {
            char c = dni.charAt(i);
            if (!isAsciiDigit(c)) {
                return false;
            }
            number = number * 10 + (c - '0');
        }
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    /** Format text@text.xxx, checked with split and String operations. */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String host = parts[1];
        int dot = host.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String extension = host.substring(dot + 1);
        return extension.length() == 3;
    }

    /** At least one uppercase letter, one lowercase letter, one digit and one symbol. */
    public static boolean isValidPassword(String password) {
        return password != null
                && containsAny(password, Character::isUpperCase)
                && containsAny(password, Character::isLowerCase)
                && containsAny(password, UserValidator::isAsciiDigit)
                && containsAny(password, UserValidator::isSymbol);
    }

    private static boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private static boolean containsAny(String text, CharCheck check) {
        for (int i = 0; i < text.length(); i++) {
            if (check.test(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface CharCheck {
        boolean test(char c);
    }

    /** Exactly 16 consecutive digits. */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        for (int i = 0; i < cardNumber.length(); i++) {
            if (!isAsciiDigit(cardNumber.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** True when DNI, email, password and card number are all valid. The telephone is not checked. */
    public static boolean isValid(User user) {
        return user != null
                && isValidDni(user.getDni())
                && isValidEmail(user.getEmail())
                && isValidPassword(user.getPassword())
                && isValidCardNumber(user.getCardNumber());
    }
}
