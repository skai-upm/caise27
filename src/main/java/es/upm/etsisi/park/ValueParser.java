package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parses command values (dates, prices, capacities) with plain String operations.
 */
public final class ValueParser {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_DECIMALS = 2;
    private static final int MAX_CAPACITY_DIGITS = 9;

    private ValueParser() {
    }

    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE);
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date (expected yyyy-MM-dd): " + value);
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE);
    }

    /**
     * Accepts digits with an optional '.' followed by one or two decimals, strictly positive.
     */
    public static BigDecimal parsePrice(String value) {
        int dot = value.indexOf('.');
        String units = dot < 0 ? value : value.substring(0, dot);
        String decimals = dot < 0 ? "" : value.substring(dot + 1);
        boolean wellFormed = !units.isEmpty() && isDigits(units)
                && (dot < 0 || (!decimals.isEmpty() && decimals.length() <= MAX_DECIMALS && isDigits(decimals)));
        if (!wellFormed) {
            throw new ParkException("Invalid price: " + value);
        }
        BigDecimal price = new BigDecimal(value);
        if (price.signum() <= 0) {
            throw new ParkException("Price must be positive: " + value);
        }
        return Money.of(price);
    }

    public static int parseCapacity(String value) {
        if (value.isEmpty() || value.length() > MAX_CAPACITY_DIGITS || !isDigits(value)) {
            throw new ParkException("Invalid capacity: " + value);
        }
        int capacity = Integer.parseInt(value);
        if (capacity <= 0) {
            throw new ParkException("Capacity must be positive: " + value);
        }
        return capacity;
    }

    private static boolean isDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
