package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Singleton holding the configurable system state: current date and current base ticket price.
 */
public final class SystemController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static SystemController instance;

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = DEFAULT_PRICE;

    private SystemController() {
    }

    public static synchronized SystemController getInstance() {
        if (instance == null) {
            instance = new SystemController();
        }
        return instance;
    }

    /**
     * Resets the state for a new execution: the given start date and the default price.
     */
    public void initialize(LocalDate startDate) {
        currentDate = startDate;
        currentPrice = DEFAULT_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public LocalDate setCurrentDate(String text) {
        currentDate = parseDate(text);
        return currentDate;
    }

    public BigDecimal setCurrentPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Invalid price: " + text
                    + ". It must be a positive amount with at most 2 decimals");
        }
        currentPrice = price.setScale(2);
        return currentPrice;
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date: " + text + ". Expected format yyyy-MM-dd");
        }
    }
}
