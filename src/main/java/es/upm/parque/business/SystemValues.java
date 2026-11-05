package es.upm.parque.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parsing of the values accepted by the SYSTEM commands. No regular expressions are used.
 */
public final class SystemValues {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_DECIMALS = 2;

    private SystemValues() {
    }

    /** Parses a {@code yyyy-MM-dd} calendar date, or returns {@code null} if it is not valid. */
    public static LocalDate parseDate(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a positive monetary amount made of digits and an optional decimal point with at most
     * two decimals, or returns {@code null} if it is not valid. The result has scale 2.
     */
    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty() || !hasMoneyShape(text)) {
            return null;
        }
        BigDecimal value = new BigDecimal(text);
        return value.signum() > 0 ? value.setScale(MAX_DECIMALS) : null;
    }

    private static boolean hasMoneyShape(String text) {
        int dot = text.indexOf('.');
        if (dot != text.lastIndexOf('.') || dot == 0 || dot == text.length() - 1) {
            return false;
        }
        if (dot > 0 && text.length() - dot - 1 > MAX_DECIMALS) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i != dot && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }
}
