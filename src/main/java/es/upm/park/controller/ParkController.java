package es.upm.park.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Central park management. Exactly one instance exists per execution; it keeps
 * the overall park state, including the current system date and base ticket price.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal basePrice;

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the startup state (local date, 40.00 euros). Intended for tests. */
    void reset() {
        currentDate = LocalDate.now();
        basePrice = INITIAL_BASE_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("The system date is required");
        }
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("The price must be a positive amount");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("The price must have at most two decimals");
        }
        basePrice = price.setScale(2);
    }
}
