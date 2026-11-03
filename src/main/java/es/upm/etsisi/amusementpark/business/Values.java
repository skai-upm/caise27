package es.upm.etsisi.amusementpark.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Parsing of command values (dates, prices, capacities) without regular expressions.
 */
public final class Values {

    private static final int MAX_CAPACITY_DIGITS = 9;
    private static final int MAX_PRICE_DECIMALS = 2;

    private Values() {
    }

    /** Parses an ISO yyyy-MM-dd date, or returns null when invalid. */
    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a positive monetary amount with at most two decimals (e.g. 45.50),
     * or returns null when invalid.
     */
    public static BigDecimal parsePrice(String value) {
        int dot = value.indexOf('.');
        String integerPart = dot < 0 ? value : value.substring(0, dot);
        String decimalPart = dot < 0 ? "" : value.substring(dot + 1);
        boolean validDecimals = dot < 0 || (isDigits(decimalPart) && decimalPart.length() <= MAX_PRICE_DECIMALS);
        if (!isDigits(integerPart) || !validDecimals) {
            return null;
        }
        BigDecimal price = new BigDecimal(value).setScale(2);
        return price.signum() > 0 ? price : null;
    }

    /** Parses a positive integer capacity, or returns -1 when invalid. */
    public static int parseCapacity(String value) {
        if (!isDigits(value) || value.length() > MAX_CAPACITY_DIGITS) {
            return -1;
        }
        int capacity = Integer.parseInt(value);
        return capacity > 0 ? capacity : -1;
    }

    private static boolean isDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
