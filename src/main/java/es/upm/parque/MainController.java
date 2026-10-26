package es.upm.parque;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Application-wide state shared by every component. Holds the single simulated current date;
 * time-dependent rules must read the date from here, never from the real clock.
 */
public class MainController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate currentDate;

    /**
     * Starts with the clock's current date; this is the only place the real date is read.
     */
    public MainController(Clock clock) {
        this.currentDate = LocalDate.now(clock);
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    /**
     * Parses a date written as yyyy-MM-dd, rejecting other formats and impossible dates.
     */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
