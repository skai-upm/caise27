package es.upm.etsisi.park.business.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.ticket.Money;

/**
 * Park-wide settings: the current system date and base ticket price.
 * Singleton; neither value is persisted.
 */
public final class SystemController {

    public static final BigDecimal DEFAULT_PRICE = Money.of(new BigDecimal("40"));

    private static final SystemController INSTANCE = new SystemController();
    private static final int DATE_LENGTH = 10;

    private LocalDate systemDate;
    private BigDecimal basePrice;

    private SystemController() {
        reset();
    }

    public static SystemController getInstance() {
        return INSTANCE;
    }

    /** Today's local date and the default price, as at the start of every execution. */
    public void reset() {
        systemDate = LocalDate.now();
        basePrice = DEFAULT_PRICE;
    }

    public LocalDate getDate() {
        return systemDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public LocalDate setDate(String text) throws ValidationException {
        systemDate = parseDate(text);
        return systemDate;
    }

    public BigDecimal setBasePrice(String text) throws ValidationException {
        BigDecimal price = Money.parsePositive(text);
        if (price == null) {
            throw new ValidationException("Invalid price '" + text + "': expected a positive amount with at most 2 decimals");
        }
        basePrice = price;
        return basePrice;
    }

    /** Strict {@code yyyy-MM-dd} calendar date. */
    public static LocalDate parseDate(String text) throws ValidationException {
        if (text == null || text.length() != DATE_LENGTH) {
            throw new ValidationException("Invalid date '" + text + "': expected yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date '" + text + "': expected yyyy-MM-dd");
        }
    }
}
