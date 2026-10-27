package es.upm.amusementpark.presentation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Arguments of one command line (elements after the command name), with syntax checks
 * and conversions done with basic String operations.
 */
final class CommandInput {

    private final String[] parts;

    CommandInput(String[] parts) {
        this.parts = parts;
    }

    /** Requires exactly {@code count} non-empty arguments, otherwise reports the expected syntax. */
    CommandInput expect(int count, String syntax) throws InvalidCommandException {
        if (parts.length != count + 1) {
            throw new InvalidCommandException("Invalid format. Use " + syntax);
        }
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new InvalidCommandException("Invalid format. Use " + syntax);
            }
        }
        return this;
    }

    /** Number of elements including the command name. */
    int size() {
        return parts.length;
    }

    /** Argument {@code index}, counting from 1. */
    String get(int index) {
        return parts[index];
    }

    /** A yyyy-MM-dd date. */
    LocalDate date(int index, String error) throws InvalidCommandException {
        String text = parts[index];
        if (text.length() == 10) {
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                // reported below
            }
        }
        throw new InvalidCommandException(error);
    }

    /** Digits with an optional '.' and at most two decimals. */
    BigDecimal amount(int index, String error) throws InvalidCommandException {
        String text = parts[index];
        int dot = text.indexOf('.');
        String integer = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integer.isEmpty() || !digits(integer) || !digits(decimals) || decimals.length() > 2
                || (dot >= 0 && decimals.isEmpty())) {
            throw new InvalidCommandException(error);
        }
        return new BigDecimal(text).setScale(2);
    }

    /** Whole number written with digits only (at most nine). */
    int number(int index, String error) throws InvalidCommandException {
        String text = parts[index];
        if (text.isEmpty() || text.length() > 9 || !digits(text)) {
            throw new InvalidCommandException(error);
        }
        return Integer.parseInt(text);
    }

    private static boolean digits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
