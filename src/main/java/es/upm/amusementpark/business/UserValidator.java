package es.upm.amusementpark.business;

/**
 * User data rules (DNI, e-mail, password, card), implemented with basic String operations only.
 */
public final class UserValidator {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private UserValidator() {
    }

    /** @return whether {@code dni} has eight digits and the correct uppercase check letter */
    public static boolean isValidDni(String dni) {
        if (dni == null || dni.length() != 9 || !allDigits(dni.substring(0, 8))) {
            return false;
        }
        int number = Integer.parseInt(dni.substring(0, 8));
        return dni.charAt(8) == DNI_LETTERS.charAt(number % 23);
    }

    /** @return whether {@code email} has the form {@code text@text.xxx} */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        for (int i = 0; i < email.length(); i++) {
            if (Character.isWhitespace(email.charAt(i))) {
                return false;
            }
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }
        String domainPart = parts[1];
        int dot = domainPart.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String domain = domainPart.substring(0, dot);
        String extension = domainPart.substring(dot + 1);
        return !domain.isEmpty() && extension.length() == 3;
    }

    /** @return whether {@code password} has an uppercase letter, a lowercase letter, a digit and a symbol */
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
            } else if (!Character.isWhitespace(c) && !Character.isLetter(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }

    /** @return whether {@code card} is exactly 16 consecutive digits */
    public static boolean isValidCard(String card) {
        return card != null && card.length() == 16 && allDigits(card);
    }

    /**
     * Applies every user data rule.
     *
     * @throws ParkException naming the first violated rule
     */
    public static void validate(String dni, String email, String password, String card) {
        if (!isValidDni(dni)) {
            throw new ParkException("Invalid DNI: eight digits and the correct check letter are required");
        }
        if (!isValidEmail(email)) {
            throw new ParkException("Invalid email: expected text@text.xxx");
        }
        if (!isValidPassword(password)) {
            throw new ParkException(
                    "Invalid password: an uppercase letter, a lowercase letter, a digit and a symbol are required");
        }
        if (!isValidCard(card)) {
            throw new ParkException("Invalid card number: exactly 16 digits are required");
        }
    }

    private static boolean allDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
