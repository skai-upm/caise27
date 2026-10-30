package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.persistence.AttractionRepository;
import es.upm.etsisi.amusementpark.persistence.GroupRepository;
import es.upm.etsisi.amusementpark.persistence.PassRepository;
import es.upm.etsisi.amusementpark.persistence.Repositories;
import es.upm.etsisi.amusementpark.persistence.TicketRepository;
import es.upm.etsisi.amusementpark.persistence.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Central park management (Singleton): owns the system date and base ticket price and gives access to
 * the injected repositories of users, groups, tickets, attractions and passes.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate today;
    private BigDecimal basePrice;
    private Repositories repositories;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repositories for this execution and resets date and price to their start values.
     */
    public void configure(Repositories repositories) {
        this.repositories = repositories;
        this.today = LocalDate.now();
        this.basePrice = INITIAL_PRICE;
    }

    public LocalDate today() {
        return today;
    }

    public BigDecimal basePrice() {
        return basePrice;
    }

    public UserRepository users() {
        return repositories.users();
    }

    public GroupRepository groups() {
        return repositories.groups();
    }

    public TicketRepository tickets() {
        return repositories.tickets();
    }

    public AttractionRepository attractions() {
        return repositories.attractions();
    }

    public PassRepository passes() {
        return repositories.passes();
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
            throw new ValidationException("Invalid price: " + text);
        }
        if (price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Price must be a positive amount with at most two decimals: " + text);
        }
        basePrice = price.setScale(2);
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }
}
