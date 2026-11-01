package es.upm.amusementpark.business.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import es.upm.amusementpark.business.OperationException;

/**
 * Holds the single system date and base ticket price of an execution. Every
 * date- or price-dependent rule reads them from here. One instance per execution.
 */
public final class SystemController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static SystemController instance;

    private LocalDate currentDate;
    private BigDecimal basePrice = DEFAULT_PRICE;

    private SystemController(LocalDate startDate) {
        this.currentDate = startDate;
    }

    /** Creates the execution's instance, starting on the given date with the default price. */
    public static synchronized SystemController initialize(LocalDate startDate) {
        if (instance != null) {
            throw new IllegalStateException("SystemController is already initialized");
        }
        instance = new SystemController(startDate);
        return instance;
    }

    public static synchronized SystemController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SystemController is not initialized");
        }
        return instance;
    }

    /** Ends the execution's instance so a new execution can initialize its own. */
    public static synchronized void release() {
        instance = null;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new OperationException("price must be greater than zero");
        }
        this.basePrice = price.setScale(2, RoundingMode.HALF_UP);
    }
}
