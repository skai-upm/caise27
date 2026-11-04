package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Common abstraction of every park ticket: an identifier, a date of use and a price. */
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

    /** The price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** The users this ticket admits; each ticket type decides who they are. */
    public abstract List<User> getVisitors();

    /** True if the ticket's date is on or after {@code day} (current or future ticket). */
    public boolean isOnOrAfter(LocalDate day) {
        return !date.isBefore(day);
    }
}
