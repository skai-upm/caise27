package es.upm.amusementpark.model;

/**
 * Validation rules for user data. Implemented without regular expressions.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int DNI_DIGITS = 8;
    private static final int CARD_DIGITS = 16;
    private static final int EMAIL_EXTENSION_LENGTH = 3;

    private UserValidator() {
    }

    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != DNI_DIGITS + 1) {
            return false;
        }
        String number = dni.substring(0, DNI_DIGITS);
        if (!allDigits(number)) {
            return false;
        }
        char letter = Character.toUpperCase(dni.charAt(DNI_DIGITS));
        return letter == DNI_LETTERS.charAt(Integer.parseInt(number) % DNI_LETTERS.length());
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] atParts = email.split("@", -1);
        if (atParts.length != 2 || atParts[0].isEmpty()) {
            return false;
        }
        String domainPart = atParts[1];
        int dot = domainPart.indexOf('.');
        if (dot < 0 || dot != domainPart.lastIndexOf('.')) {
            return false;
        }
        String domain = domainPart.substring(0, dot);
        String extension = domainPart.substring(dot + 1);
        return !domain.isEmpty() && extension.length() == EMAIL_EXTENSION_LENGTH;
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

    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.length() == CARD_DIGITS && allDigits(cardNumber);
    }

    /**
     * Checks every validated attribute of a user. The telephone number is not validated.
     */
    public static boolean isValid(User user) {
        return user != null
                && isValidDni(user.getDni())
                && isValidEmail(user.getEmail())
                && isValidPassword(user.getPassword())
                && isValidCardNumber(user.getCardNumber());
    }

    private static boolean allDigits(String s) {
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
