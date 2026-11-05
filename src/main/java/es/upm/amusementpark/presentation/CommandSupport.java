package es.upm.amusementpark.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Parsing helpers shared by command handlers, based on basic String operations only.
 */
final class CommandSupport {

    private CommandSupport() {
    }

    /**
     * Splits a command line into trimmed fields.
     *
     * @throws InvalidCommandException if the number of fields differs from {@code expected}
     */
    static String[] fields(String line, int expected, String usage) {
        String[] f = line.split(";", -1);
        if (f.length != expected) {
            throw new InvalidCommandException("Invalid syntax. Usage: " + usage);
        }
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].trim();
        }
        return f;
    }

    /**
     * Parses a strict {@code yyyy-MM-dd} date.
     *
     * @return the date, or {@code null} if invalid
     */
    static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a ticket date.
     *
     * @throws InvalidCommandException if invalid
     */
    static LocalDate ticketDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            throw new InvalidCommandException("Invalid date. Expected yyyy-MM-dd");
        }
        return date;
    }
}
