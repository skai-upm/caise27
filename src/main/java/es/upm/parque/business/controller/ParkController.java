package es.upm.parque.business.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Main controller of the park (Singleton). It owns the single system date used by every time-dependent rule and
 * the current individual ticket price.
 */
public final class ParkController {

    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int DATE_LENGTH = 10;

    private static ParkController instance;

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    private ParkController() {
    }

    public static synchronized ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /**
     * Starts a run: the system date becomes {@code startDate} and the price returns to its default.
     */
    public void configure(LocalDate startDate) {
        this.currentDate = startDate;
        this.ticketPrice = DEFAULT_TICKET_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    /**
     * Parses a date in the exact format yyyy-MM-dd, returning {@code null} if it is not a valid date.
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != DATE_LENGTH) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a positive amount with at most two decimals, returning {@code null} otherwise.
     */
    public static BigDecimal parsePrice(String text) {
        try {
            BigDecimal price = new BigDecimal(text);
            if (price.signum() <= 0 || price.scale() > 2) {
                return null;
            }
            return price.setScale(2);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    public String setSystemDate(String text) {
        LocalDate date = parseDate(text);
        if (date == null) {
            return "ERROR Invalid date: " + text + " (expected yyyy-MM-dd)";
        }
        currentDate = date;
        return "OK SYSTEM DATE " + currentDate;
    }

    public String setTicketPrice(String text) {
        BigDecimal price = parsePrice(text);
        if (price == null) {
            return "ERROR Invalid price: " + text + " (expected a positive amount with up to 2 decimals)";
        }
        ticketPrice = price;
        return "OK SYSTEM PRICE " + ticketPrice.toPlainString();
    }
}
