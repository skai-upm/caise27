package es.upm.amusementpark.park;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.repository.UserRepository;

/**
 * Central park management. Exactly one instance exists per execution; it coordinates
 * the park state (users, and later groups, tickets and attractions), the current system
 * date and the current base ticket price.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static ParkController instance;

    private UserRepository users;
    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        start(new UserRepository());
    }

    public static ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /** Initialises the state of a new execution: today's date and the initial price. */
    public void start(UserRepository users) {
        this.users = users;
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}
