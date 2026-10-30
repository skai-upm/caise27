package es.upm.amusementpark;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Central park management. Only one instance exists per execution; it holds the current system date and base
 * price and gives access to every repository of the park.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static Park instance;

    private LocalDate date;
    private BigDecimal price;
    private UserRepository users;
    private GroupRepository groups;
    private AttractionRepository attractions;
    private TicketRepository tickets;

    private Park() {
    }

    public static synchronized Park getInstance() {
        if (instance == null) {
            instance = new Park();
        }
        return instance;
    }

    /**
     * Loads the park stored in the given data directory and resets the date to today and the price to 40.00.
     */
    public void open(Path dataDirectory) {
        users = new UserRepository(dataDirectory.resolve(UserRepository.DEFAULT_FILE));
        users.load();
        groups = new GroupRepository(dataDirectory.resolve(GroupRepository.DEFAULT_FILE), users);
        groups.load();
        attractions = new AttractionRepository(dataDirectory.resolve(AttractionRepository.DEFAULT_FILE));
        attractions.load();
        tickets = new TicketRepository(dataDirectory.resolve(TicketRepository.DEFAULT_FILE), users, groups);
        tickets.load();
        date = LocalDate.now();
        price = INITIAL_PRICE;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public AttractionRepository getAttractions() {
        return attractions;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    /**
     * A group is locked while it has a group ticket dated on or after the current system date.
     */
    public boolean isLocked(Group group) {
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            if (ticket.isActiveOn(date)) {
                return true;
            }
        }
        return false;
    }
}
