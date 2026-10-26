package es.upm.parque.business.controller;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Application-wide state shared by every component. Holds the single simulated current date
 * (time-dependent rules must read it from here, never from the real clock) and the current
 * individual ticket price. Neither value is persisted.
 */
public final class MainController {

    /** Individual ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    private MainController() {
    }

    /**
     * Returns the single instance of this controller.
     */
    public static MainController getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Starts the execution state: the clock's current date (the only place the real date is read)
     * and the default ticket price.
     */
    public void configure(Clock clock) {
        this.currentDate = LocalDate.now(clock);
        this.ticketPrice = DEFAULT_TICKET_PRICE;
    }

    public LocalDate getCurrentDate() {
        requireConfigured();
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        requireConfigured();
        this.currentDate = date;
    }

    public BigDecimal getTicketPrice() {
        requireConfigured();
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal price) {
        requireConfigured();
        this.ticketPrice = price;
    }

    private void requireConfigured() {
        if (currentDate == null) {
            throw new IllegalStateException("MainController is not configured");
        }
    }

    private static final class Holder {
        static final MainController INSTANCE = new MainController();
    }
}
