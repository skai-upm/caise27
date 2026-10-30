package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.InvalidDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parsing and formatting of dates, prices and capacities used by park commands. No regular expressions.
 */
public final class ParkFormat {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private ParkFormat() {
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("invalid date " + text + " (expected yyyy-MM-dd)");
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE);
    }

    /**
     * A positive monetary amount: digits, optionally followed by '.' and one or two decimal digits.
     */
    public static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimalPart = dot < 0 ? "" : text.substring(dot + 1);
        boolean wellFormed = !integerPart.isEmpty() && allDigits(integerPart)
                && (dot < 0 || (!decimalPart.isEmpty() && decimalPart.length() <= 2 && allDigits(decimalPart)));
        if (!wellFormed) {
            throw new InvalidDataException("invalid price " + text + " (expected an amount such as 45.50)");
        }
        BigDecimal price = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        if (price.signum() <= 0) {
            throw new InvalidDataException("the price must be greater than zero");
        }
        return price;
    }

    public static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * A positive whole number.
     */
    public static int parseCapacity(String text) {
        if (text.isEmpty() || text.length() > 9 || !allDigits(text) || Integer.parseInt(text) == 0) {
            throw new InvalidDataException("invalid capacity " + text + " (expected a positive whole number)");
        }
        return Integer.parseInt(text);
    }

    private static boolean allDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
