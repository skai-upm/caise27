package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Owns the park-wide settings: the current system date and the current base
 * ticket price.
 */
public final class SystemController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final SystemController INSTANCE = new SystemController();

    private LocalDate today;
    private BigDecimal price;

    private SystemController() {
    }

    public static SystemController getInstance() {
        return INSTANCE;
    }

    /** Starts a run on {@code startDate} with the default price. */
    public void configure(LocalDate startDate) {
        this.today = startDate;
        this.price = DEFAULT_PRICE;
    }

    public LocalDate getDate() {
        return today;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setDate(String text) throws InvalidDataException {
        today = parseDate(text);
    }

    public void setPrice(String text) throws InvalidDataException {
        price = parsePrice(text);
    }

    /** Parses a strict ISO {@code yyyy-MM-dd} date. */
    public static LocalDate parseDate(String text) throws InvalidDataException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("Invalid date '" + text + "' (expected an existing date as yyyy-MM-dd)");
        }
    }

    /** Parses a positive amount with at most two decimals, such as {@code 45.50}. */
    public static BigDecimal parsePrice(String text) throws InvalidDataException {
        if (!isPlainAmount(text)) {
            throw new InvalidDataException("Invalid price '" + text + "' (expected a positive amount such as 45.50)");
        }
        BigDecimal value = new BigDecimal(text);
        if (value.signum() <= 0) {
            throw new InvalidDataException("Invalid price '" + text + "' (the price must be greater than zero)");
        }
        return value.setScale(2);
    }

    private static boolean isPlainAmount(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int dot = text.indexOf('.');
        if (dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1) {
            return false;
        }
        if (dot > 0 && text.length() - dot - 1 > 2) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '.' && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
