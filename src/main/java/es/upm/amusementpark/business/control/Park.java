package es.upm.amusementpark.business.control;

import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
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
    private PassRepository passes;

    private Park() {
        start(Repositories.inMemory());
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Initialises a new execution with the injected repositories: today's date and the initial price.
     */
    public void start(Repositories repositories) {
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
        this.users = repositories.users();
        this.groups = repositories.groups();
        this.tickets = repositories.tickets();
        this.attractions = repositories.attractions();
        this.passes = repositories.passes();
    }

    public GroupRepository groups() {
        return groups;
    }

    public TicketRepository tickets() {
        return tickets;
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    public PassRepository passes() {
        return passes;
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
