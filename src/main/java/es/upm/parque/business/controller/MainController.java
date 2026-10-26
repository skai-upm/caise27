package es.upm.parque.business.controller;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Application-wide state shared by every component. Holds the single simulated current date
 * (time-dependent rules must read it from here, never from the real clock) and the current
 * individual ticket price. Neither value is persisted.
 */
public class MainController {

    /** Individual ticket price at the start of every execution. */
    public static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    /**
     * Starts with the clock's current date; this is the only place the real date is read.
     */
    public MainController(Clock clock) {
        this.currentDate = LocalDate.now(clock);
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal price) {
        this.ticketPrice = price;
    }
}
