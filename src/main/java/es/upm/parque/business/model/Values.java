package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parsing and formatting of the dates and amounts used in commands, output and stored files.
 */
public final class Values {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private Values() {
    }

    /**
     * Parses a positive amount with at most two decimals, normalised to two decimals.
     */
    public static BigDecimal parsePrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new ParkException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new ParkException("Invalid price (positive, at most two decimals): " + text);
        }
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Writes an amount with two decimals and '.' as separator, independent of the locale.
     */
    public static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Parses a date written as yyyy-MM-dd, rejecting other formats and impossible dates.
     */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
