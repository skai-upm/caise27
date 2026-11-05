package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Central, single-instance manager of the overall park state during one execution.
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        reset();
    }

    /**
     * Returns the only central park management instance.
     *
     * @return the singleton instance
     */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state: today's local date and the initial base price. */
    public synchronized void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
    }

    public synchronized LocalDate getCurrentDate() {
        return currentDate;
    }

    public synchronized void setCurrentDate(LocalDate date) {
        currentDate = Objects.requireNonNull(date, "date");
    }

    public synchronized BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the base ticket price for tickets created from now on.
     *
     * @param price strictly positive amount
     */
    public synchronized void setCurrentPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        currentPrice = price.setScale(2, RoundingMode.UNNECESSARY);
    }
}
