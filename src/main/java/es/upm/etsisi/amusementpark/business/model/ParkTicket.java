package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Common abstraction of every ticket sold by the park: an identifier, a date of use and a price. */
public abstract class ParkTicket {

    private final String id;
    private final LocalDate date;

    protected ParkTicket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    /** True if the ticket is dated on or after {@code today} (current or future). */
    public boolean isValidOn(LocalDate today) {
        return !date.isBefore(today);
    }

    public abstract BigDecimal getPrice();

    /** The registered users this ticket admits: its owner, or every member of a group ticket. */
    public abstract List<User> getVisitors();
}
