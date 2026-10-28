package es.upm.etsisi.amusementpark.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Single central element maintaining the overall park state during an execution.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state. */
    void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Replaces the current system date.
     *
     * @throws IllegalArgumentException if the text is not a valid {@code yyyy-MM-dd} date
     */
    public void setCurrentDate(String text) {
        currentDate = parseDate(text);
    }

    /**
     * Replaces the current base ticket price.
     *
     * @throws IllegalArgumentException if the text is not a valid, positive monetary amount
     */
    public void setCurrentPrice(String text) {
        currentPrice = parsePrice(text);
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            throw new IllegalArgumentException("Invalid date, expected yyyy-MM-dd: " + text);
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || !isDigits(integerPart) || (dot >= 0 && (decimals.isEmpty()
                || decimals.length() > 2 || !isDigits(decimals)))) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Invalid price, it must be positive: " + text);
        }
        return price;
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i)) || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
