package es.upm.parque.business.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Main controller of the application: owns the single system date and the current ticket price.
 * Neither is persisted. Singleton.
 */
public final class ParkController {

    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate systemDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a new execution with the given date and the default price.
     */
    public void configure(LocalDate initialDate) {
        this.systemDate = initialDate;
        this.ticketPrice = DEFAULT_TICKET_PRICE;
    }

    public LocalDate getSystemDate() {
        return systemDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public LocalDate setSystemDate(String text) {
        systemDate = parseDate(text);
        return systemDate;
    }

    public BigDecimal setTicketPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + text);
        }
        ticketPrice = price.setScale(2, RoundingMode.HALF_UP);
        return ticketPrice;
    }

    /**
     * Parses an ISO {@code yyyy-MM-dd} date strictly.
     */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }
}
