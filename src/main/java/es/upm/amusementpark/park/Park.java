package es.upm.amusementpark.park;

import es.upm.amusementpark.repository.AttractionRepository;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Central manager of the park state. Exactly one instance exists per execution.
 */
public final class Park {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;

    private Park() {
        start(new UserRepository());
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Initialises the state of a new execution: today's date, the initial price and the given users.
     */
    public void start(UserRepository userRepository) {
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
        this.users = userRepository;
        this.groups = new GroupRepository();
        this.tickets = new TicketRepository();
        this.attractions = new AttractionRepository();
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    public TicketRepository tickets() {
        return tickets;
    }

    public GroupRepository groups() {
        return groups;
    }

    public UserRepository users() {
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

    public void setCurrentPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        this.currentPrice = price.setScale(2, RoundingMode.UNNECESSARY);
    }
}
