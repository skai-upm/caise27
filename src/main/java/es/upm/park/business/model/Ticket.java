package es.upm.park.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of every ticket: an identifier, a date of use and a price.
 */
public abstract class Ticket implements Admissible {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    /** True if the ticket is dated on or after {@code day} (current or future). */
    public boolean isOnOrAfter(LocalDate day) {
        return !date.isBefore(day);
    }

    /** Users admitted with this ticket; {@code null} entries stand for deleted users. */
    @Override
    public abstract List<User> getHolders();

    /** Price paid for this ticket. */
    public abstract BigDecimal getPrice();
}
