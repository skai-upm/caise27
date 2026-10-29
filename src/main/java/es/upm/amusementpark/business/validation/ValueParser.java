package es.upm.amusementpark.business.validation;

import es.upm.amusementpark.business.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parses command values (dates, prices, capacities) without regular expressions.
 */
public final class ValueParser {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_PRICE_DECIMALS = 2;

    private ValueParser() {
    }

    /**
     * Parses a {@code yyyy-MM-dd} date, rejecting impossible dates such as 2027-02-30.
     */
    public static LocalDate parseDate(String value) throws ValidationException {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("invalid date, expected yyyy-MM-dd: " + value);
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    /**
     * Parses a positive amount with digits, an optional '.' and at most two decimals.
     */
    public static BigDecimal parsePrice(String value) throws ValidationException {
        int dot = value.indexOf('.');
        String integerPart = dot < 0 ? value : value.substring(0, dot);
        String decimalPart = dot < 0 ? "" : value.substring(dot + 1);
        boolean wellFormed = isDigits(integerPart)
                && (dot < 0 || (isDigits(decimalPart) && decimalPart.length() <= MAX_PRICE_DECIMALS));
        if (!wellFormed) {
            throw new ValidationException("invalid price: " + value);
        }
        BigDecimal price = new BigDecimal(value).setScale(MAX_PRICE_DECIMALS);
        if (price.signum() <= 0) {
            throw new ValidationException("price must be positive: " + value);
        }
        return price;
    }

    /**
     * Parses a strictly positive integer made only of digits.
     */
    public static int parsePositiveInt(String value) throws ValidationException {
        if (!isDigits(value)) {
            throw new ValidationException("invalid number: " + value);
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("number too large: " + value);
        }
        if (number <= 0) {
            throw new ValidationException("number must be positive: " + value);
        }
        return number;
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
