package es.upm.etsisi.park.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Execution-wide settings that park operations depend on: the current date and the
 * base ticket price. Neither is persisted.
 */
public class SystemState {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");
    public static final int PRICE_SCALE = 2;

    private LocalDate currentDate;
    private BigDecimal basePrice;

    public SystemState(LocalDate currentDate) {
        this.currentDate = currentDate;
        this.basePrice = INITIAL_PRICE;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * @throws IllegalArgumentException when the price is not positive or has more than two decimals
     */
    public void setBasePrice(BigDecimal price) {
        if (price.signum() <= 0 || price.stripTrailingZeros().scale() > PRICE_SCALE) {
            throw new IllegalArgumentException("Price must be a positive amount with at most two decimals");
        }
        this.basePrice = price.setScale(PRICE_SCALE, RoundingMode.UNNECESSARY);
    }
}
