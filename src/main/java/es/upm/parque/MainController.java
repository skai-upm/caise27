package es.upm.parque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Application-wide state shared by every component. Holds the single simulated current date
 * (time-dependent rules must read it from here, never from the real clock) and the current
 * individual ticket price. Neither value is persisted.
 */
public class MainController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Individual ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

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

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal price) {
        this.ticketPrice = price;
    }

    /**
     * Parses a positive amount with at most two decimals, normalised to two decimals.
     */
    public static BigDecimal parsePrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new ParkException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new ParkException("Invalid price (positive, at most two decimals): " + text);
        }
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Writes an amount with two decimals and '.' as separator, independent of the locale.
     */
    public static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
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
