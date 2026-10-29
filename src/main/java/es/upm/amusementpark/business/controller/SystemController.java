package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.validation.ValueParser;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Holds the current system date and base ticket price for one execution (never persisted).
 */
public final class SystemController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final SystemController INSTANCE = new SystemController();

    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;

    private SystemController() {
    }

    public static SystemController getInstance() {
        return INSTANCE;
    }

    /**
     * Start-of-execution state: the given date and a price of 40.00.
     */
    public void reset(LocalDate today) {
        currentDate = today;
        currentPrice = INITIAL_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentDate(String value) throws ValidationException {
        currentDate = ValueParser.parseDate(value);
    }

    public void setCurrentPrice(String value) throws ValidationException {
        currentPrice = ValueParser.parsePrice(value);
    }
}
