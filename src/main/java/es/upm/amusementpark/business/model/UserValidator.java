package es.upm.amusementpark.business.model;

/**
 * Validation rules for user data. No regular expressions are used.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    /**
     * A DNI is eight digits followed by the control letter matching the number.
     */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1 || !allDigits(dni.substring(0, DNI_DIGITS))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, DNI_DIGITS));
        return dni.charAt(DNI_DIGITS) == DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /**
     * An email has the form {@code text@text.xxx}: content before and after a single {@code @}, a
     * domain, and an extension of exactly three characters.
     */
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
        return dot > 0 && host.length() - dot - 1 == EMAIL_EXTENSION_LENGTH;
    }

    /**
     * A password contains at least one uppercase letter, one lowercase letter, one digit and one
     * symbol.
     */
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

    /**
     * A card number is exactly sixteen consecutive digits.
     */
    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.length() == CARD_DIGITS && allDigits(cardNumber);
    }

    /**
     * A user is valid when its DNI, email, password and card number are valid. The telephone number
     * is not validated.
     */
    public static boolean isValid(User user) {
        return user != null && isValidDni(user.getDni()) && isValidEmail(user.getEmail())
                && isValidPassword(user.getPassword()) && isValidCardNumber(user.getCardNumber());
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
