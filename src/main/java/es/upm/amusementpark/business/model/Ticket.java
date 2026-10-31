package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets. Date rules and the
 * users a ticket admits are expressed here so that callers can handle every
 * ticket type uniformly.
 */
public abstract class Ticket implements AccessTicket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Users admitted by this ticket. */
    public abstract List<User> getHolders();

    /** Current or future with respect to the given date: blocks deletions and locks groups. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Usable to enter an attraction on the given date. */
    @Override
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** Strictly in the future with respect to the given date. */
    public boolean isRefundableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
