package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Runtime system settings: current date and base ticket price. Neither is persisted.
 */
public final class SystemController {

    private static final int MAX_DECIMALS = 2;
    private static final SystemController INSTANCE = new SystemController();

    private Park park;

    private SystemController() {
    }

    public static SystemController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    public LocalDate setDate(String text) throws ValidationException {
        LocalDate date = parseDate(text);
        park.setCurrentDate(date);
        return date;
    }

    public BigDecimal setPrice(String text) throws ValidationException {
        BigDecimal price = parsePrice(text);
        park.setBasePrice(price);
        return price;
    }

    /** Parses an ISO {@code yyyy-MM-dd} date. */
    public static LocalDate parseDate(String text) throws ValidationException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("invalid date " + text + ": expected yyyy-MM-dd");
        }
    }

    /** Parses a positive monetary amount with at most two decimals, without regular expressions. */
    static BigDecimal parsePrice(String text) throws ValidationException {
        if (!isMonetaryAmount(text)) {
            throw new ValidationException("invalid price " + text + ": expected a positive amount such as 45.50");
        }
        BigDecimal price = new BigDecimal(text).setScale(MAX_DECIMALS, RoundingMode.UNNECESSARY);
        if (price.signum() <= 0) {
            throw new ValidationException("invalid price " + text + ": it must be positive");
        }
        return price;
    }

    private static boolean isMonetaryAmount(String text) {
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (whole.isEmpty() || !isDigits(whole) || !isDigits(decimals)) {
            return false;
        }
        return dot < 0 || (!decimals.isEmpty() && decimals.length() <= MAX_DECIMALS);
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
