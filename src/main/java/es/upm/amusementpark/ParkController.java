package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Central park management. Only one instance exists per execution; it keeps
 * the current system date and the current base ticket price.
 */
public final class ParkController {

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate currentDate;
    private BigDecimal basePrice;

    private ParkController() {
        reset();
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Restores the start-of-execution state (used by tests). */
    void reset() {
        currentDate = LocalDate.now();
        basePrice = DEFAULT_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the current system date from a {@code yyyy-MM-dd} text.
     *
     * @throws IllegalArgumentException if the text is not a valid date in that format
     */
    public void setCurrentDate(String text) {
        try {
            currentDate = LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date (expected yyyy-MM-dd): " + text, e);
        }
    }

    /**
     * Sets the current base ticket price.
     *
     * @throws IllegalArgumentException if the text is not a positive amount with at most two decimals
     */
    public void setBasePrice(String text) {
        basePrice = parsePrice(text.trim());
    }

    private static BigDecimal parsePrice(String text) {
        int digits = 0;
        int decimals = 0;
        boolean seenPoint = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.' && !seenPoint) {
                seenPoint = true;
            } else if (c >= '0' && c <= '9') {
                digits++;
                if (seenPoint) {
                    decimals++;
                }
            } else {
                throw new IllegalArgumentException("invalid price: " + text);
            }
        }
        if (digits == 0 || decimals > 2 || text.startsWith(".") || text.endsWith(".")) {
            throw new IllegalArgumentException("invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive: " + text);
        }
        return price;
    }
}
