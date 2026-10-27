package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Central management of the park state during one execution: the users, the
 * current system date and the current base ticket price. Only one instance
 * exists.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserRepository users = new UserRepository();
    private LocalDate currentDate = LocalDate.now();
    private BigDecimal currentPrice = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares the park state for a new execution.
     */
    public void initialize(UserRepository users, LocalDate today) {
        this.users = users;
        this.currentDate = today;
        this.currentPrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Replaces the current base ticket price.
     *
     * @throws IllegalArgumentException if the price is not positive
     */
    public void setCurrentPrice(BigDecimal price) {
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("The price must be positive.");
        }
        this.currentPrice = price.setScale(2);
    }
}
