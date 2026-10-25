package es.upm.parque;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * The application's single simulated current date. Time-dependent rules must read it
 * instead of the computer's clock.
 */
public class SystemDate {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate current;

    public SystemDate(LocalDate initial) {
        this.current = initial;
    }

    public LocalDate get() {
        return current;
    }

    public void set(LocalDate date) {
        this.current = date;
    }

    /**
     * Parses a yyyy-MM-dd calendar date, returning null when the text is not a valid date.
     */
    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String format(LocalDate date) {
        return FORMAT.format(date);
    }
}
