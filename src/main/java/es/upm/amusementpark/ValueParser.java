package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parsing of command values (dates, prices, capacities) without regular expressions.
 */
public final class ValueParser {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private ValueParser() {
    }

    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date (expected yyyy-MM-dd): " + value);
        }
    }

    /** A positive amount: digits, optionally a '.' followed by one or two digits. */
    public static BigDecimal parsePrice(String value) {
        int dot = value.indexOf('.');
        String units = dot < 0 ? value : value.substring(0, dot);
        String cents = dot < 0 ? "" : value.substring(dot + 1);
        boolean wellFormed = !units.isEmpty() && isDigits(units)
                && (dot < 0 || (!cents.isEmpty() && cents.length() <= 2 && isDigits(cents)));
        if (!wellFormed) {
            throw new IllegalArgumentException("invalid price: " + value);
        }
        BigDecimal price = new BigDecimal(value).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive: " + value);
        }
        return price;
    }

    public static int parsePositiveInt(String value, String name) {
        if (value.isEmpty() || value.length() > 9 || !isDigits(value) || Integer.parseInt(value) <= 0) {
            throw new IllegalArgumentException("invalid " + name + ": " + value);
        }
        return Integer.parseInt(value);
    }

    private static boolean isDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
