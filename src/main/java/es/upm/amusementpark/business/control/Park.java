package es.upm.amusementpark.business.control;

import es.upm.amusementpark.persistence.storage.AttractionFileStorage;
import es.upm.amusementpark.persistence.storage.GroupFileStorage;
import es.upm.amusementpark.persistence.storage.TicketFileStorage;
import es.upm.amusementpark.persistence.storage.UserFileStorage;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
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
        start(userRepository, new GroupRepository(), new TicketRepository(), new AttractionRepository());
    }

    /**
     * Initialises a new execution whose state is persisted in text files inside {@code dataDirectory}.
     * Repositories are loaded in dependency order: users, groups, tickets, attractions.
     */
    public void start(Path dataDirectory) {
        UserRepository u = new UserRepository(new UserFileStorage(dataDirectory.resolve("users.txt")));
        GroupRepository g = new GroupRepository(new GroupFileStorage(dataDirectory.resolve("groups.txt")), u);
        TicketRepository t = new TicketRepository(new TicketFileStorage(dataDirectory.resolve("tickets.txt")), u, g);
        AttractionRepository a = new AttractionRepository(new AttractionFileStorage(dataDirectory.resolve("attractions.txt")));
        start(u, g, t, a);
    }

    private void start(UserRepository u, GroupRepository g, TicketRepository t, AttractionRepository a) {
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
        this.users = u;
        this.groups = g;
        this.tickets = t;
        this.attractions = a;
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
