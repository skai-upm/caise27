package es.upm.park.presentation;

import es.upm.park.business.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parses command values (dates, prices, capacities, quantities) using
 * character scanning and the java.time API; regular expressions are not used.
 */
public final class ValueParser {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_PRICE_DECIMALS = 2;
    private static final int MAX_CAPACITY_DIGITS = 9;

    private ValueParser() {
    }

    public static LocalDate parseDate(String text) throws ValidationException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    /**
     * Parses a positive monetary amount with at most two decimal places,
     * such as {@code 40}, {@code 45.5} or {@code 45.50}.
     */
    public static BigDecimal parsePrice(String text) throws ValidationException {
        if (!isDecimalAmount(text)) {
            throw new ValidationException("Invalid price (expected a positive amount with up to 2 decimals): " + text);
        }
        BigDecimal price = new BigDecimal(text);
        if (price.signum() <= 0) {
            throw new ValidationException("The price must be greater than zero: " + text);
        }
        return price.setScale(MAX_PRICE_DECIMALS);
    }

    public static int parseCapacity(String text) throws ValidationException {
        if (text == null || text.isEmpty() || text.length() > MAX_CAPACITY_DIGITS || !isAllDigits(text)) {
            throw new ValidationException("Invalid capacity (expected a positive whole number): " + text);
        }
        int capacity = Integer.parseInt(text);
        if (capacity <= 0) {
            throw new ValidationException("The capacity must be greater than zero: " + text);
        }
        return capacity;
    }

    /**
     * Parses a non-negative whole number such as a pass quantity.
     */
    public static int parseQuantity(String text) throws ValidationException {
        if (text == null || text.isEmpty() || text.length() > MAX_CAPACITY_DIGITS || !isAllDigits(text)) {
            throw new ValidationException("Invalid quantity (expected a whole number): " + text);
        }
        return Integer.parseInt(text);
    }

    private static boolean isDecimalAmount(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimalPart = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || !isAllDigits(integerPart) || !isAllDigits(decimalPart)) {
            return false;
        }
        if (dot >= 0 && decimalPart.isEmpty()) {
            return false;
        }
        return decimalPart.length() <= MAX_PRICE_DECIMALS;
    }

    private static boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
