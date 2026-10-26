package es.upm.parque.business.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * The application's single current date. Every time-dependent rule must use it instead of the real clock.
 */
public class SystemDate {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate current;

    public SystemDate(LocalDate initial) {
        this.current = initial;
    }

    /**
     * Creates the system date starting at the operating system's current date.
     */
    public static SystemDate today() {
        return new SystemDate(LocalDate.now());
    }

    public LocalDate get() {
        return current;
    }

    public void set(LocalDate date) {
        this.current = date;
    }

    /**
     * Parses a date in yyyy-MM-dd format, rejecting dates that do not exist.
     *
     * @throws IllegalArgumentException if the text is not a valid date
     */
    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + text + " (expected yyyy-MM-dd)", e);
        }
    }

    public static String format(LocalDate date) {
        return date.format(FORMAT);
    }
}
