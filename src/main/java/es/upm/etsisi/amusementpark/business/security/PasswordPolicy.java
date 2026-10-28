package es.upm.etsisi.amusementpark.business.security;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;

/**
 * Delivery 1 password rule: at least one uppercase letter, one lowercase letter,
 * one digit and one symbol (a character that is not a letter, digit or whitespace).
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    /**
     * @throws InvalidValueException if the password does not satisfy the rule
     */
    public static void validate(String password) {
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        if (password != null) {
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
        }
        if (!(upper && lower && digit && symbol)) {
            throw new InvalidValueException("Invalid password: it must contain at least one uppercase letter, "
                    + "one lowercase letter, one digit and one symbol");
        }
    }
}
