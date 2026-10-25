package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Users that this ticket admits.
     */
    public abstract List<User> getUsers();

    public abstract BigDecimal getPrice();

    /**
     * A ticket is active (current or future) when its date is on or after the given date.
     */
    public boolean isActiveOn(LocalDate currentDate) {
        return !date.isBefore(currentDate);
    }

    /**
     * Tickets can only be deleted when their date is strictly later than the given date.
     */
    public boolean isDeletableOn(LocalDate currentDate) {
        return date.isAfter(currentDate);
    }
}
