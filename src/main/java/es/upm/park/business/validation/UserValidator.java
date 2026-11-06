package es.upm.park.business.validation;

import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.security.PasswordPolicy;

/**
 * Checks the data that defines a user (DNI, email, password, card) with plain
 * String operations, without regular expressions or external libraries.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /** Eight digits and the check letter of the Spanish DNI algorithm. */
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

    /** {@code text@text.xxx}: content before @, a domain, and a three-character extension. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || containsWhitespace(email)) {
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
        String domain = host.substring(0, dot);
        String extension = host.substring(dot + 1);
        return !domain.isEmpty() && !domain.startsWith(".") && !domain.endsWith(".") && extension.length() == 3;
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
            } else if (!Character.isWhitespace(c) && !Character.isLetter(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /** Exactly 16 consecutive digits. */
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

    public static void requireValidDni(String dni) {
        require(isValidDni(dni), "Invalid DNI: " + dni);
    }

    public static void requireValidEmail(String email) {
        require(isValidEmail(email), "Invalid email: " + email);
    }

    public static void requireValidPassword(String password) {
        require(isValidPassword(password),
                "Invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
    }

    public static void requireValidCard(String card) {
        require(isValidCard(card), "Invalid card number: it must have exactly 16 digits");
    }

    public static void requireText(String value, String field) {
        require(value != null && !value.isBlank(), "Invalid " + field + ": it is required");
    }

    /** Validates all the data of a user, reporting the first invalid field. */
    public static void validateUser(String firstName, String surnames, String dni, String email, String password,
                                    String phone, String card) {
        requireText(firstName, "first name");
        requireText(surnames, "surnames");
        requireValidDni(dni);
        requireValidEmail(email);
        requireValidPassword(password);
        requireText(phone, "phone");
        requireValidCard(card);
    }

    /** The password rule as the policy applied before passwords are protected. */
    public static PasswordPolicy passwordPolicy() {
        return UserValidator::requireValidPassword;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidInputException(message);
        }
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
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
