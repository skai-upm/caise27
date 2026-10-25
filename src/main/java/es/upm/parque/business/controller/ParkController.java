package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Main controller of the park (Singleton). It holds the single system date and the current
 * individual ticket price; neither is persisted.
 */
public final class ParkController {

    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal ticketPrice;

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Restores the start-of-execution values: today's date and the default price.
     */
    public void reset() {
        currentDate = LocalDate.now(ZoneId.systemDefault());
        ticketPrice = DEFAULT_TICKET_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public LocalDate setSystemDate(String text) {
        currentDate = parseDate(text);
        return currentDate;
    }

    public BigDecimal setTicketPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new ParkException("invalid price " + text);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new ParkException("price must be positive with at most two decimals: " + text);
        }
        ticketPrice = price.setScale(2, RoundingMode.UNNECESSARY);
        return ticketPrice;
    }

    /**
     * Parses a strict {@code yyyy-MM-dd} date.
     */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ParkException("invalid date " + text + ", expected yyyy-MM-dd");
        }
    }
}
