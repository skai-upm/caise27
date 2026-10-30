package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.repository.AttractionFileStore;
import es.upm.etsisi.amusementpark.repository.AttractionRepository;
import es.upm.etsisi.amusementpark.repository.GroupFileStore;
import es.upm.etsisi.amusementpark.repository.GroupRepository;
import es.upm.etsisi.amusementpark.repository.TicketFileStore;
import es.upm.etsisi.amusementpark.repository.TicketRepository;
import es.upm.etsisi.amusementpark.repository.UserFileStore;
import es.upm.etsisi.amusementpark.repository.UserRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Central park management: the single owner of the system date, the base ticket price and
 * the repositories of users, groups, tickets and attractions.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate today;
    private BigDecimal basePrice;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Loads every repository from the given data directory and resets date and price to their
     * start-of-execution values.
     */
    public void initialize(Path dataDir) {
        users = new UserRepository(new UserFileStore(dataDir.resolve("users.txt")));
        attractions = new AttractionRepository(new AttractionFileStore(dataDir.resolve("attractions.txt")));
        groups = new GroupRepository(new GroupFileStore(dataDir.resolve("groups.txt"), users));
        tickets = new TicketRepository(new TicketFileStore(dataDir.resolve("tickets.txt"), users, groups));
        today = LocalDate.now();
        basePrice = INITIAL_PRICE;
    }

    public LocalDate today() {
        return today;
    }

    public BigDecimal basePrice() {
        return basePrice;
    }

    public UserRepository users() {
        return users;
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

    /**
     * SYSTEM DATE: replaces the current system date.
     */
    public void setDate(String text) {
        today = parseDate(text);
    }

    /**
     * SYSTEM PRICE: replaces the base price used for tickets created from now on.
     */
    public void setPrice(String text) {
        BigDecimal price;
        try {
            price = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Price must be a positive amount with at most two decimals: " + text);
        }
        basePrice = price.setScale(2);
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }
}
