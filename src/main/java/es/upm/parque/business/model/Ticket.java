package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket sold by the park: it has an identifier, a date of use and a price.
 * A ticket is valid for entering attractions only on its date of use.
 */
public abstract class Ticket implements Admission {

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

    @Override
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    public abstract BigDecimal getPrice();
}
