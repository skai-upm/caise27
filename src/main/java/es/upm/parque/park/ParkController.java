package es.upm.parque.park;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.parque.repository.UserRepository;

/**
 * Central park management: the single object that coordinates the overall park state
 * during an execution (users, current system date and current base ticket price).
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private UserRepository users = new UserRepository();
    private LocalDate currentDate = LocalDate.now();
    private BigDecimal basePrice = INITIAL_PRICE;

    private ParkController() {
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    /** Returns the only central park management instance. */
    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Starts a new execution: uses the given users and resets the date to today and the price to 40.00. */
    public void startExecution(UserRepository users) {
        this.users = users;
        this.currentDate = LocalDate.now();
        this.basePrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
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

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
