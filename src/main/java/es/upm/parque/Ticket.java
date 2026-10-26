package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket sold by the park: it has an identifier, a date of use and a price.
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

    /** @return true if the ticket can still be used on or after {@code today} */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    public abstract BigDecimal getPrice();
}
