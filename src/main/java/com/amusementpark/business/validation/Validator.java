package com.amusementpark.business.validation;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Validation rules for user data, implemented without regular expressions.
 */
public final class Validator {
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private Validator() {
    }

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
        char letter = Character.toUpperCase(dni.charAt(8));
        return letter == DNI_LETTERS.charAt(number % 23);
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
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

    /**
     * Identifiers of groups, tickets and attractions: letters, digits, '-' and '_'.
     */
    public static boolean isValidId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (!isAsciiDigit(c) && !Character.isLetter(c) && c != '-' && c != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a strict yyyy-MM-dd date, returning null when it is not valid.
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Parses a positive amount with at most two decimals, returning null when it is not valid.
     */
    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimalPart = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || decimalPart.length() > 2 || (dot >= 0 && decimalPart.isEmpty())
                || !allDigits(integerPart) || !allDigits(decimalPart)) {
            return null;
        }
        BigDecimal price = new BigDecimal(text);
        return price.signum() > 0 ? price : null;
    }

    /**
     * Parses a positive integer capacity, returning -1 when it is not valid.
     */
    public static int parseCapacity(String text) {
        if (text == null || text.isEmpty() || text.length() > 9 || !allDigits(text)) {
            return -1;
        }
        int capacity = Integer.parseInt(text);
        return capacity > 0 ? capacity : -1;
    }

    private static boolean allDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!isAsciiDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
